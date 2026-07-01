package com.consult.reservation.service;

import com.consult.reservation.dto.DeviceTokenRegisterRequest;
import com.consult.reservation.entity.DeviceToken;
import com.consult.reservation.repository.DeviceTokenRepository;
import com.consult.reservation.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    /** Flutter FCM 토큰 등록/갱신 */
    @Transactional
    public void register(DeviceTokenRegisterRequest request) {
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId를 입력해주세요.");
        }
        if (request.getFcmToken() == null || request.getFcmToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fcmToken을 입력해주세요.");
        }
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        String fcmToken = request.getFcmToken().trim();
        DeviceToken deviceToken = deviceTokenRepository.findByFcmToken(fcmToken)
                .orElseGet(DeviceToken::new);

        deviceToken.setUserId(request.getUserId());
        deviceToken.setFcmToken(fcmToken);
        deviceToken.setPlatform(request.getPlatform());
        deviceToken.setUpdatedAt(LocalDateTime.now());

        deviceTokenRepository.save(deviceToken);
        log.info("[FCM] 토큰 등록 완료 userId={}, platform={}, tokenId={}",
                deviceToken.getUserId(), deviceToken.getPlatform(), deviceToken.getId());
    }
}
