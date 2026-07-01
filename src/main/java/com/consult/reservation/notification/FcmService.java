package com.consult.reservation.notification;

import com.consult.reservation.dto.BookingResponse;
import com.consult.reservation.entity.DeviceToken;
import com.consult.reservation.repository.DeviceTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final DeviceTokenRepository deviceTokenRepository;

    /** userId에 등록된 모든 기기로 예약 알림 push */
    public void sendBookingUpdated(Long userId, BookingResponse booking) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            log.warn("[FCM] userId={} 에 등록된 토큰 없음 — Flutter에서 POST /api/devices/token 호출 필요", userId);
            return;
        }

        String title = buildTitle(booking.getStatus());
        String body = buildBody(booking);
        Map<String, String> data = buildData(booking);

        for (DeviceToken deviceToken : tokens) {
            sendToToken(deviceToken, title, body, data);
        }
    }

    private void sendToToken(DeviceToken deviceToken, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(deviceToken.getFcmToken())
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("[FCM] push 성공 userId={}, tokenId={}, response={}",
                    deviceToken.getUserId(), deviceToken.getId(), response);
        } catch (FirebaseMessagingException ex) {
            log.warn("[FCM] push 실패 userId={}, tokenId={}, code={}, message={}, cause={}",
                    deviceToken.getUserId(), deviceToken.getId(),
                    ex.getMessagingErrorCode(), ex.getMessage(),
                    ex.getCause() != null ? ex.getCause().getMessage() : null);

            if (isInvalidToken(ex)) {
                deviceTokenRepository.deleteByFcmToken(deviceToken.getFcmToken());
            }
        }
    }

    private boolean isInvalidToken(FirebaseMessagingException ex) {
        MessagingErrorCode code = ex.getMessagingErrorCode();
        return code == MessagingErrorCode.UNREGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT;
    }

    private String buildTitle(String status) {
        return switch (status) {
            case "PENDING" -> "새 예약 요청";
            case "ACCEPTED" -> "예약 확정";
            case "REJECTED" -> "예약 거절";
            case "CANCELLED" -> "예약 취소";
            default -> "예약 알림";
        };
    }

    private String buildBody(BookingResponse booking) {
        return booking.getDate() + " " + booking.getTimeSlot()
                + " (" + booking.getStatus() + ")";
    }

    private Map<String, String> buildData(BookingResponse booking) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "booking-updated");
        data.put("bookingId", String.valueOf(booking.getId()));
        data.put("clientId", String.valueOf(booking.getClientId()));
        data.put("counselorId", String.valueOf(booking.getCounselorId()));
        data.put("status", booking.getStatus());
        data.put("date", booking.getDate());
        data.put("timeSlot", booking.getTimeSlot());
        return data;
    }
}
