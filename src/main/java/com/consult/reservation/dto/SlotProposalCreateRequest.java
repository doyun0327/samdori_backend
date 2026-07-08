package com.consult.reservation.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** POST /api/slot-proposals 요청 DTO */
@Getter
@Setter
@NoArgsConstructor
public class SlotProposalCreateRequest {

    private Long counselorId;
    private Long clientId;
    private List<ProposalSlotRequest> slots;
    private String message;
}
