package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PATCH /api/bookings/{bookingId}/cancel 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class BookingCancelRequest {

    private Long clientId;
    private Long counselorId;
    private String reason;
}
