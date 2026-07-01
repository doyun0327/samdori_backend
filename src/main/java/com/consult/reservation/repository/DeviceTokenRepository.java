package com.consult.reservation.repository;

import com.consult.reservation.entity.DeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserId(Long userId);

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    void deleteByFcmToken(String fcmToken);
}
