package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PATCH cancel 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class BookingCancelRequest {

    private Long clientId;
}
