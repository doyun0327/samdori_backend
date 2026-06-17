package com.consult.reservation.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 상담 가능 시간 등록 결과 DTO */
@Getter
@RequiredArgsConstructor
public class AvailabilityResponse {

    /** 로그인 UserResponse.id 와 동일한 값 */
    private final Long id;
    private final String date;
    private final List<String> timeSlots;
}
