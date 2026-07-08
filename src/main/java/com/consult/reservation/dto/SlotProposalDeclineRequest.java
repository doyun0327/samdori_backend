package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PATCH /api/slot-proposals/{proposalId}/decline 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class SlotProposalDeclineRequest {

    private Long clientId;
    private String date;
    private String timeSlot;
}
