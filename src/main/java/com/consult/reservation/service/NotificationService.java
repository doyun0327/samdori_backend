package com.consult.reservation.service;

import com.consult.reservation.dto.BookingResponse;
import com.consult.reservation.dto.SlotProposalResponse;
import com.consult.reservation.notification.FcmService;
import com.consult.reservation.notification.SseEmitterRegistry;
import com.consult.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String CLIENT_ROLE = "CLIENT";
    private static final String COUNSELOR_ROLE = "COUNSELOR";

    private final SseEmitterRegistry sseEmitterRegistry;
    private final FcmService fcmService;
    private final UserRepository userRepository;

    public SseEmitter subscribe(Long userId, String role) {
        validateSubscriber(userId, role);
        return sseEmitterRegistry.connect(userId, role);
    }

    /** booking-updated: SSE(Web) + FCM(Flutter) */
    public void sendBookingUpdated(Long userId, String role, BookingResponse booking) {
        sseEmitterRegistry.send(
                userId,
                role,
                SseEmitterRegistry.EVENT_BOOKING_UPDATED,
                booking
        );
        fcmService.sendBookingUpdated(userId, role, booking);
    }

    /** slot-proposal-updated: SSE(Web) + FCM(Flutter) */
    public void sendSlotProposalUpdated(Long userId, String role, SlotProposalResponse proposal) {
        sseEmitterRegistry.send(
                userId,
                role,
                SseEmitterRegistry.EVENT_SLOT_PROPOSAL_UPDATED,
                proposal
        );
        fcmService.sendSlotProposalUpdated(userId, proposal);
    }

    private void validateSubscriber(Long userId, String role) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId를 입력해주세요.");
        }
        if (role == null || role.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role을 입력해주세요.");
        }

        String normalizedRole = role.toUpperCase();
        if (!CLIENT_ROLE.equals(normalizedRole) && !COUNSELOR_ROLE.equals(normalizedRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role은 CLIENT 또는 COUNSELOR 이어야 합니다.");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
    }
}
