package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** POST /api/slot-proposals/{id}/book 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class SlotProposalBookRequest {

    private Long clientId;
    private String date;
    private String timeSlot;
}
