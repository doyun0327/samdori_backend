package com.consult.reservation.service;

import com.consult.reservation.dto.BookingCancelRequest;
import com.consult.reservation.dto.BookingCounselorActionRequest;
import com.consult.reservation.dto.BookingCreateRequest;
import com.consult.reservation.dto.BookingResponse;
import com.consult.reservation.entity.Booking;
import com.consult.reservation.entity.BookingStatus;
import com.consult.reservation.entity.CancelledBy;
import com.consult.reservation.entity.User;
import com.consult.reservation.repository.BookingRepository;
import com.consult.reservation.repository.CounselorAvailabilityRepository;
import com.consult.reservation.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String COUNSELOR_ROLE = "COUNSELOR";
    private static final String CLIENT_ROLE = "CLIENT";
    private static final List<BookingStatus> CANCELLABLE_STATUSES = List.of(
            BookingStatus.PENDING, BookingStatus.ACCEPTED);

    private final BookingRepository bookingRepository;
    private final CounselorAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /** 내담자 예약 요청 생성 */
    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        validateCreateRequest(request);

        Long clientId = request.getClientId();
        Long counselorId = request.getCounselorId();
        LocalDate date = parseDate(request.getDate());
        String timeSlot = request.getTimeSlot().trim();

        User client = findUser(clientId, "존재하지 않는 내담자입니다.");
        User counselor = findCounselor(counselorId);

        if (!availabilityRepository.existsByCounselorIdAndAvailabilityDateAndTimeSlot(
                counselorId, date, timeSlot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상담사가 오픈하지 않은 시간입니다.");
        }

        if (bookingRepository.existsByCounselorIdAndDateAndTimeSlotAndStatusIn(
                counselorId, date, timeSlot, CANCELLABLE_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 예약된 시간입니다.");
        }

        Booking booking = new Booking();
        booking.setClientId(clientId);
        booking.setCounselorId(counselorId);
        booking.setDate(date);
        booking.setTimeSlot(timeSlot);
        booking.setStatus(BookingStatus.PENDING);
        booking.setRequestedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        BookingResponse response = toResponse(saved, client, counselor);
        notifyCounselor(response);
        return response;
    }

    /** 내담자 예약 목록 */
    @Transactional(readOnly = true)
    public List<BookingResponse> findByClientId(Long clientId) {
        validateUserId(clientId, "clientId");
        return toResponses(bookingRepository.findByClientIdOrderByRequestedAtDesc(clientId));
    }

    /** 상담사 예약 요청 목록 */
    @Transactional(readOnly = true)
    public List<BookingResponse> findByCounselorId(Long counselorId) {
        validateUserId(counselorId, "counselorId");
        findCounselor(counselorId);
        return toResponses(bookingRepository.findByCounselorIdOrderByRequestedAtDesc(counselorId));
    }

    /** 상담사 예약 수락 */
    @Transactional
    public BookingResponse accept(Long bookingId, BookingCounselorActionRequest request) {
        Booking booking = findPendingBookingForCounselor(bookingId, request.getCounselorId());
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setRespondedAt(LocalDateTime.now());
        BookingResponse response = toResponse(booking);
        notifyClient(response);
        return response;
    }

    /** 상담사 예약 거절 */
    @Transactional
    public BookingResponse reject(Long bookingId, BookingCounselorActionRequest request) {
        Booking booking = findPendingBookingForCounselor(bookingId, request.getCounselorId());
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRespondedAt(LocalDateTime.now());
        BookingResponse response = toResponse(booking);
        notifyClient(response);
        return response;
    }

    /** 예약 취소 — PENDING·ACCEPTED, 아직 지나지 않은 일정만 */
    @Transactional
    public BookingResponse cancel(Long bookingId, BookingCancelRequest request) {
        validateCancelRequest(request);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."));

        CancelledBy cancelledBy = resolveCancelledBy(request, booking);
        validateCancellable(booking);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason(request.getReason().trim());
        booking.setCancelledBy(cancelledBy);

        BookingResponse response = toResponse(booking);
        if (cancelledBy == CancelledBy.CLIENT) {
            notifyCounselor(response);
        } else {
            notifyClient(response);
        }
        return response;
    }

    private void validateCancelRequest(BookingCancelRequest request) {
        boolean hasClientId = request.getClientId() != null;
        boolean hasCounselorId = request.getCounselorId() != null;

        if (hasClientId == hasCounselorId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "clientId 또는 counselorId 중 하나만 입력해주세요.");
        }
        if (isBlank(request.getReason())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reason을 입력해주세요.");
        }
    }

    private CancelledBy resolveCancelledBy(BookingCancelRequest request, Booking booking) {
        if (request.getClientId() != null) {
            if (!booking.getClientId().equals(request.getClientId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "예약을 취소할 권한이 없습니다.");
            }
            return CancelledBy.CLIENT;
        }

        if (!booking.getCounselorId().equals(request.getCounselorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "예약을 취소할 권한이 없습니다.");
        }
        findCounselor(request.getCounselorId());
        return CancelledBy.COUNSELOR;
    }

    private void validateCancellable(Booking booking) {
        if (!CANCELLABLE_STATUSES.contains(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "취소할 수 없는 예약 상태입니다.");
        }
        if (!isFutureSchedule(booking)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    private boolean isFutureSchedule(Booking booking) {
        LocalTime slotStart = parseSlotStartTime(booking.getTimeSlot());
        LocalDateTime scheduleAt = LocalDateTime.of(booking.getDate(), slotStart);
        return scheduleAt.isAfter(LocalDateTime.now());
    }

    private LocalTime parseSlotStartTime(String timeSlot) {
        String start = timeSlot.trim().split("-")[0].trim();
        try {
            String[] segments = start.split(":");
            int hour = Integer.parseInt(segments[0]);
            int minute = segments.length > 1 ? Integer.parseInt(segments[1]) : 0;
            return LocalTime.of(hour, minute);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeSlot 형식이 올바르지 않습니다.");
        }
    }

    private Booking findPendingBookingForCounselor(Long bookingId, Long counselorId) {
        if (counselorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "counselorId를 입력해주세요.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."));

        if (!booking.getCounselorId().equals(counselorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "예약을 처리할 권한이 없습니다.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대기 중인 예약만 처리할 수 있습니다.");
        }

        return booking;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateCreateRequest(BookingCreateRequest request) {
        validateUserId(request.getClientId(), "clientId");
        validateUserId(request.getCounselorId(), "counselorId");

        if (request.getDate() == null || request.getDate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date를 입력해주세요.");
        }
        if (request.getTimeSlot() == null || request.getTimeSlot().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeSlot을 입력해주세요.");
        }
    }

    private void validateUserId(Long id, String fieldName) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "를 입력해주세요.");
        }
    }

    private User findUser(Long id, String notFoundMessage) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, notFoundMessage));
    }

    private User findCounselor(Long counselorId) {
        User counselor = findUser(counselorId, "존재하지 않는 상담사입니다.");
        if (!COUNSELOR_ROLE.equals(counselor.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상담사만 예약 대상이 될 수 있습니다.");
        }
        return counselor;
    }

    private List<BookingResponse> toResponses(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return List.of();
        }
        Map<Long, User> users = loadUsers(bookings);
        return bookings.stream()
                .map(booking -> toResponse(
                        booking,
                        requireUser(users, booking.getClientId(), "존재하지 않는 내담자입니다."),
                        requireUser(users, booking.getCounselorId(), "존재하지 않는 상담사입니다.")))
                .toList();
    }

    private Map<Long, User> loadUsers(List<Booking> bookings) {
        Set<Long> ids = new HashSet<>();
        for (Booking booking : bookings) {
            ids.add(booking.getClientId());
            ids.add(booking.getCounselorId());
        }
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private User requireUser(Map<Long, User> users, Long id, String notFoundMessage) {
        User user = users.get(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFoundMessage);
        }
        return user;
    }

    private BookingResponse toResponse(Booking booking) {
        return toResponses(List.of(booking)).get(0);
    }

    private BookingResponse toResponse(Booking booking, User client, User counselor) {
        return new BookingResponse(booking, client, counselor);
    }

    /** 내담자 예약 상태 변경 알림 전송 */
    private void notifyClient(BookingResponse booking) {
        notificationService.sendBookingUpdated(booking.getClientId(), CLIENT_ROLE, booking);
    }

    /** 상담사 예약 상태 변경 알림 전송 */
    private void notifyCounselor(BookingResponse booking) {
        notificationService.sendBookingUpdated(booking.getCounselorId(), COUNSELOR_ROLE, booking);
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date 형식은 yyyy-MM-dd 입니다.");
        }
    }
}
