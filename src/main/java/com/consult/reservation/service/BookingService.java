package com.consult.reservation.service;

import com.consult.reservation.dto.BookingCancelRequest;
import com.consult.reservation.dto.BookingCounselorActionRequest;
import com.consult.reservation.dto.BookingCreateRequest;
import com.consult.reservation.dto.BookingResponse;
import com.consult.reservation.entity.Booking;
import com.consult.reservation.entity.BookingStatus;
import com.consult.reservation.entity.User;
import com.consult.reservation.repository.BookingRepository;
import com.consult.reservation.repository.CounselorAvailabilityRepository;
import com.consult.reservation.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
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
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
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
                counselorId, date, timeSlot, ACTIVE_STATUSES)) {
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
    public List<BookingResponse> findByClientId(Long clientId) {
        validateUserId(clientId, "clientId");

        return bookingRepository.findByClientIdOrderByRequestedAtDesc(clientId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 상담사 예약 요청 목록 */
    public List<BookingResponse> findByCounselorId(Long counselorId) {
        validateUserId(counselorId, "counselorId");
        findCounselor(counselorId);

        return bookingRepository.findByCounselorIdOrderByRequestedAtDesc(counselorId).stream()
                .map(this::toResponse)
                .toList();
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

    /** 내담자 예약 취소 */
    @Transactional
    public BookingResponse cancel(Long bookingId, BookingCancelRequest request) {
        if (request.getClientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId를 입력해주세요.");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 예약입니다."));

        if (!booking.getClientId().equals(request.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "예약을 취소할 권한이 없습니다.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대기 중인 예약만 취소할 수 있습니다.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        BookingResponse response = toResponse(booking);
        notifyCounselor(response);
        return response;
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

    private BookingResponse toResponse(Booking booking) {
        User client = findUser(booking.getClientId(), "존재하지 않는 내담자입니다.");
        User counselor = findUser(booking.getCounselorId(), "존재하지 않는 상담사입니다.");
        return toResponse(booking, client, counselor);
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
