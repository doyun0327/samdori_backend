package com.consult.reservation.dto;

import com.consult.reservation.entity.ProposalSlot;
import com.consult.reservation.entity.ProposalSlotStatus;
import com.consult.reservation.entity.SlotProposal;
import com.consult.reservation.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class SlotProposalResponse {

    private final Long id;
    private final Long counselorId;
    private final String counselorName;
    private final Long clientId;
    private final String clientName;
    private final String message;
    private final String status;
    private final List<ProposalSlotResponse> slots;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;

    public SlotProposalResponse(SlotProposal proposal, User counselor, User client) {
        this.id = proposal.getId();
        this.counselorId = proposal.getCounselorId();
        this.counselorName = counselor.getName();
        this.clientId = proposal.getClientId();
        this.clientName = client.getName();
        this.message = proposal.getMessage();
        this.status = proposal.getStatus().name();
        this.slots = proposal.getSlots().stream()
                .map(SlotProposalResponse::toSlotResponse)
                .toList();
        this.createdAt = proposal.getCreatedAt();
        this.expiresAt = proposal.getExpiresAt();
    }

    private static ProposalSlotResponse toSlotResponse(ProposalSlot slot) {
        return new ProposalSlotResponse(
                slot.getDate(),
                slot.getTimeSlot(),
                slot.getEffectiveStatus().name());
    }
}
