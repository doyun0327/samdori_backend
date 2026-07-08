package com.consult.reservation.service;

import com.consult.reservation.dto.BookingResponse;
import com.consult.reservation.dto.ProposalSlotRequest;
import com.consult.reservation.dto.SlotProposalBookRequest;
import com.consult.reservation.dto.SlotProposalBookResponse;
import com.consult.reservation.dto.SlotProposalCancelRequest;
import com.consult.reservation.dto.SlotProposalDeclineRequest;
import com.consult.reservation.dto.SlotProposalCreateRequest;
import com.consult.reservation.dto.SlotProposalResponse;
import com.consult.reservation.entity.Booking;
import com.consult.reservation.entity.BookingStatus;
import com.consult.reservation.entity.ProposalSlot;
import com.consult.reservation.entity.ProposalSlotStatus;
import com.consult.reservation.entity.SlotProposal;
import com.consult.reservation.entity.SlotProposalStatus;
import com.consult.reservation.entity.User;
import com.consult.reservation.repository.BookingRepository;
import com.consult.reservation.repository.SlotProposalRepository;
import com.consult.reservation.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SlotProposalService {

    private static final String COUNSELOR_ROLE = "COUNSELOR";
    private static final String CLIENT_ROLE = "CLIENT";
    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES = List.of(
            BookingStatus.PENDING, BookingStatus.ACCEPTED);

    private final SlotProposalRepository slotProposalRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /** 상담사 → 고객 시간 제안 생성 */
    @Transactional
    public SlotProposalResponse create(SlotProposalCreateRequest request) {
        validateCreateRequest(request);

        User counselor = findCounselor(request.getCounselorId());
        User client = findClient(request.getClientId());

        List<ProposalSlot> normalizedSlots = normalizeAndValidateFutureSlots(request.getSlots());

        SlotProposal proposal = new SlotProposal();
        proposal.setCounselorId(request.getCounselorId());
        proposal.setClientId(request.getClientId());
        proposal.setMessage(blankToNull(request.getMessage()));
        proposal.setStatus(SlotProposalStatus.PENDING);
        proposal.setSlots(normalizedSlots);
        proposal.setCreatedAt(LocalDateTime.now());

        SlotProposal saved = slotProposalRepository.save(proposal);
        SlotProposalResponse response = toResponse(saved, counselor, client);
        notificationService.sendSlotProposalUpdated(client.getId(), CLIENT_ROLE, response);
        return response;
    }

    /** 고객이 받은 제안 목록 */
    public List<SlotProposalResponse> findByClientId(Long clientId) {
        validateUserId(clientId, "clientId");
        findClient(clientId);

        return slotProposalRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 상담사가 보낸 제안 목록 */
    public List<SlotProposalResponse> findByCounselorId(Long counselorId) {
        validateUserId(counselorId, "counselorId");
        findCounselor(counselorId);

        return slotProposalRepository.findByCounselorIdOrderByCreatedAtDesc(counselorId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 고객 — 슬롯 선택 후 예약 확정 */
    @Transactional
    public SlotProposalBookResponse book(Long proposalId, SlotProposalBookRequest request) {
        validateBookRequest(request);

        SlotProposal proposal = slotProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 제안입니다."));

        if (!proposal.getClientId().equals(request.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "제안을 예약할 권한이 없습니다.");
        }
        validateProposalOpen(proposal);

        LocalDate date = parseDate(request.getDate());
        String timeSlot = normalizeTimeSlot(request.getTimeSlot());
        ProposalSlot slot = findPendingSlot(proposal, date, timeSlot);
        validateFutureSchedule(date, timeSlot);

        if (bookingRepository.existsByCounselorIdAndDateAndTimeSlotAndStatusIn(
                proposal.getCounselorId(), date, timeSlot, ACTIVE_BOOKING_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 예약된 시간입니다.");
        }

        User client = findUser(proposal.getClientId(), "존재하지 않는 내담자입니다.");
        User counselor = findUser(proposal.getCounselorId(), "존재하지 않는 상담사입니다.");

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking();
        booking.setClientId(proposal.getClientId());
        booking.setCounselorId(proposal.getCounselorId());
        booking.setDate(date);
        booking.setTimeSlot(timeSlot);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setRequestedAt(now);
        booking.setRespondedAt(now);

        Booking savedBooking = bookingRepository.save(booking);

        slot.setStatus(ProposalSlotStatus.BOOKED);
        refreshProposalStatus(proposal, now);

        BookingResponse bookingResponse = new BookingResponse(savedBooking, client, counselor);
        SlotProposalResponse proposalResponse = toResponse(proposal, counselor, client);
        notificationService.sendBookingUpdated(counselor.getId(), COUNSELOR_ROLE, bookingResponse);

        return new SlotProposalBookResponse(bookingResponse, proposalResponse);
    }

    /** 고객 — 슬롯 거절 */
    @Transactional
    public SlotProposalResponse decline(Long proposalId, SlotProposalDeclineRequest request) {
        validateDeclineRequest(request);

        SlotProposal proposal = slotProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 제안입니다."));

        if (!proposal.getClientId().equals(request.getClientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "제안을 거절할 권한이 없습니다.");
        }
        validateProposalOpen(proposal);

        LocalDate date = parseDate(request.getDate());
        String timeSlot = normalizeTimeSlot(request.getTimeSlot());
        ProposalSlot slot = findPendingSlot(proposal, date, timeSlot);

        slot.setStatus(ProposalSlotStatus.DECLINED);
        refreshProposalStatus(proposal, null);

        SlotProposalResponse response = toResponse(proposal);
        notificationService.sendSlotProposalUpdated(
                proposal.getCounselorId(), COUNSELOR_ROLE, response);
        return response;
    }

    /** 상담사 — 제안 취소 */
    @Transactional
    public SlotProposalResponse cancel(Long proposalId, SlotProposalCancelRequest request) {
        if (request.getCounselorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "counselorId를 입력해주세요.");
        }

        SlotProposal proposal = slotProposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 제안입니다."));

        if (!proposal.getCounselorId().equals(request.getCounselorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "제안을 취소할 권한이 없습니다.");
        }
        validateProposalOpen(proposal);

        findCounselor(request.getCounselorId());
        for (ProposalSlot slot : proposal.getSlots()) {
            if (slot.getEffectiveStatus() == ProposalSlotStatus.PENDING) {
                slot.setStatus(ProposalSlotStatus.DECLINED);
            }
        }
        proposal.setStatus(SlotProposalStatus.CANCELLED);

        SlotProposalResponse response = toResponse(proposal);
        notificationService.sendSlotProposalUpdated(proposal.getClientId(), CLIENT_ROLE, response);
        return response;
    }

    private void validateCreateRequest(SlotProposalCreateRequest request) {
        validateUserId(request.getCounselorId(), "counselorId");
        validateUserId(request.getClientId(), "clientId");

        if (request.getSlots() == null || request.getSlots().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slots를 입력해주세요.");
        }
        if (request.getSlots().stream().anyMatch(this::isInvalidSlotRequest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slots에 date, timeSlot을 모두 입력해주세요.");
        }
    }

    private void validateDeclineRequest(SlotProposalDeclineRequest request) {
        validateUserId(request.getClientId(), "clientId");
        if (request.getDate() == null || request.getDate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date를 입력해주세요.");
        }
        if (request.getTimeSlot() == null || request.getTimeSlot().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeSlot을 입력해주세요.");
        }
    }

    private void validateBookRequest(SlotProposalBookRequest request) {
        validateUserId(request.getClientId(), "clientId");
        if (request.getDate() == null || request.getDate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date를 입력해주세요.");
        }
        if (request.getTimeSlot() == null || request.getTimeSlot().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeSlot을 입력해주세요.");
        }
    }

    private boolean isInvalidSlotRequest(ProposalSlotRequest slot) {
        return slot == null
                || slot.getDate() == null || slot.getDate().isBlank()
                || slot.getTimeSlot() == null || slot.getTimeSlot().isBlank();
    }

    private List<ProposalSlot> normalizeAndValidateFutureSlots(List<ProposalSlotRequest> slots) {
        List<ProposalSlot> normalized = new ArrayList<>();
        for (ProposalSlotRequest slot : slots) {
            LocalDate date = parseDate(slot.getDate());
            String timeSlot = normalizeTimeSlot(slot.getTimeSlot());
            validateFutureSchedule(date, timeSlot);
            normalized.add(new ProposalSlot(date.toString(), timeSlot, ProposalSlotStatus.PENDING));
        }
        return normalized;
    }

    private void validateProposalOpen(SlotProposal proposal) {
        if (proposal.getStatus() == SlotProposalStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "종료된 제안입니다.");
        }
        if (proposal.getStatus() == SlotProposalStatus.BOOKED && !hasPendingSlot(proposal)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "처리할 수 없는 제안입니다.");
        }
        if (!hasPendingSlot(proposal)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택 가능한 시간이 없습니다.");
        }
    }

    private ProposalSlot findPendingSlot(SlotProposal proposal, LocalDate date, String timeSlot) {
        String dateStr = date.toString();
        return proposal.getSlots().stream()
                .filter(slot -> dateStr.equals(slot.getDate()) && timeSlot.equals(slot.getTimeSlot()))
                .filter(slot -> slot.getEffectiveStatus() == ProposalSlotStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "선택할 수 없는 시간입니다."));
    }

    private boolean hasPendingSlot(SlotProposal proposal) {
        return proposal.getSlots().stream()
                .anyMatch(slot -> slot.getEffectiveStatus() == ProposalSlotStatus.PENDING);
    }

    private boolean hasBookedSlot(SlotProposal proposal) {
        return proposal.getSlots().stream()
                .anyMatch(slot -> slot.getEffectiveStatus() == ProposalSlotStatus.BOOKED);
    }

    private void refreshProposalStatus(SlotProposal proposal, LocalDateTime bookedAt) {
        if (hasPendingSlot(proposal)) {
            proposal.setStatus(SlotProposalStatus.PENDING);
            return;
        }
        if (hasBookedSlot(proposal)) {
            proposal.setStatus(SlotProposalStatus.BOOKED);
            if (proposal.getBookedAt() == null && bookedAt != null) {
                proposal.setBookedAt(bookedAt);
            }
            return;
        }
        proposal.setStatus(SlotProposalStatus.CANCELLED);
    }

    private void validateFutureSchedule(LocalDate date, String timeSlot) {
        LocalTime slotStart = parseSlotStartTime(timeSlot);
        LocalDateTime scheduleAt = LocalDateTime.of(date, slotStart);
        if (!scheduleAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "과거 시간은 선택할 수 없습니다.");
        }
    }

    private LocalTime parseSlotStartTime(String timeSlot) {
        String start = timeSlot.split("-")[0].trim();
        try {
            String[] segments = start.split(":");
            int hour = Integer.parseInt(segments[0]);
            int minute = segments.length > 1 ? Integer.parseInt(segments[1]) : 0;
            return LocalTime.of(hour, minute);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeSlot 형식이 올바르지 않습니다.");
        }
    }

    private String normalizeTimeSlot(String slot) {
        String trimmed = slot.trim();
        String[] parts = trimmed.split("-");
        if (parts.length != 2) {
            return normalizeTime(trimmed);
        }
        return normalizeTime(parts[0]) + "-" + normalizeTime(parts[1]);
    }

    private String normalizeTime(String time) {
        String[] segments = time.trim().split(":");
        int hour = Integer.parseInt(segments[0]);
        int minute = Integer.parseInt(segments[1]);
        return String.format("%02d:%02d", hour, minute);
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date.trim());
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date 형식은 yyyy-MM-dd 입니다.");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상담사만 제안을 보낼 수 있습니다.");
        }
        return counselor;
    }

    private User findClient(Long clientId) {
        User client = findUser(clientId, "존재하지 않는 내담자입니다.");
        if (!CLIENT_ROLE.equals(client.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "내담자에게만 제안을 보낼 수 있습니다.");
        }
        return client;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private SlotProposalResponse toResponse(SlotProposal proposal) {
        User client = findUser(proposal.getClientId(), "존재하지 않는 내담자입니다.");
        User counselor = findUser(proposal.getCounselorId(), "존재하지 않는 상담사입니다.");
        return toResponse(proposal, counselor, client);
    }

    private SlotProposalResponse toResponse(SlotProposal proposal, User counselor, User client) {
        return new SlotProposalResponse(proposal, counselor, client);
    }
}
