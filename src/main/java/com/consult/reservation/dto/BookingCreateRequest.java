package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** POST /api/bookings 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class BookingCreateRequest {

    private Long clientId;
    private Long counselorId;
    private String date;
    private String timeSlot;
}
