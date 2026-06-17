package com.consult.reservation.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** POST/DELETE /api/counselor/availability 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class AvailabilityRequest {

    /** 로그인 UserResponse.id 와 동일한 값 */
    private Long id;
    private String date;
    private List<String> timeSlots;
}
