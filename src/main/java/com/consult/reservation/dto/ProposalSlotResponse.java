package com.consult.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProposalSlotResponse {

    private final String date;
    private final String timeSlot;
    private final String status;
}
