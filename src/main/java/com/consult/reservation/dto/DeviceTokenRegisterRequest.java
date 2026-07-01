package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** POST /api/devices/token 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class DeviceTokenRegisterRequest {

    private Long userId;
    private String fcmToken;
    private String platform;
}
