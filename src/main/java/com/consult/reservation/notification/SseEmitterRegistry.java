package com.consult.reservation.notification;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRegistry {

    public static final String EVENT_BOOKING_UPDATED = "booking-updated";
    /** 0 = 서버 타임아웃 없음. 연결은 프론트에서 close() 할 때까지 유지 */
    private static final long TIMEOUT_MS = 0L;

    private final CopyOnWriteArrayList<Connection> connections = new CopyOnWriteArrayList<>();

    public SseEmitter connect(Long userId, String role) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        Connection connection = new Connection(userId, role.toUpperCase(), emitter);

        connections.add(connection);

        Runnable remove = () -> connections.remove(connection);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(ex -> remove.run());

        return emitter;
    }

    // 이미 연결된 SSE 클라이언트 중에서, 지정한 사람에게만 이벤트를 push하는 함수
    public void send(Long userId, String role, String eventName, Object data) {
        String normalizedRole = role.toUpperCase();

        for (Connection connection : connections) {
            if (!connection.matches(userId, normalizedRole)) {
                continue;
            }

            try {
                connection.emitter().send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException ex) {
                connections.remove(connection);
            }
        }
    }

    private record Connection(Long userId, String role, SseEmitter emitter) {

        boolean matches(Long targetUserId, String targetRole) {
            return userId.equals(targetUserId) && role.equals(targetRole);
        }
    }
}
