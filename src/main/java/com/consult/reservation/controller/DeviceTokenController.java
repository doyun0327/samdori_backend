package com.consult.reservation.controller;

import com.consult.reservation.dto.DeviceTokenRegisterRequest;
import com.consult.reservation.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    /** POST /api/devices/token — Flutter FCM 토큰 등록 */
    @PostMapping("/token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerToken(@RequestBody DeviceTokenRegisterRequest request) {
        deviceTokenService.register(request);
    }
}
