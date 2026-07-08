package com.consult.reservation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SlotProposalBookResponse {

    private final BookingResponse booking;
    private final SlotProposalResponse proposal;
}
