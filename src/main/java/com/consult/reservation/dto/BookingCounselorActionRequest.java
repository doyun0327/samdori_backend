package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PATCH accept/reject 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class BookingCounselorActionRequest {

    private Long counselorId;
}
