package com.consult.reservation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** API 오류 시 프론트로 반환하는 공통 응답 형식 */
@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final int status;
    private final String message;
}
