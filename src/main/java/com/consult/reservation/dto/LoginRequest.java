package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인 API 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    private String loginId;
    private String password;
}