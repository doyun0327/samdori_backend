package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PATCH /api/slot-proposals/{id}/cancel 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class SlotProposalCancelRequest {

    private Long counselorId;
}
