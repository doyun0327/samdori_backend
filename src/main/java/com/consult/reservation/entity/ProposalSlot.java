package com.consult.reservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** slot_proposals.slots JSON 요소 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalSlot {

    private String date;
    private String timeSlot;
    private ProposalSlotStatus status;

    public ProposalSlot(String date, String timeSlot) {
        this(date, timeSlot, ProposalSlotStatus.PENDING);
    }

    public ProposalSlot(String date, String timeSlot, ProposalSlotStatus status) {
        this.date = date;
        this.timeSlot = timeSlot;
        this.status = status;
    }

    /** 기존 JSON에 status 없으면 PENDING — JSON 직렬화 제외 */
    @JsonIgnore
    public ProposalSlotStatus getEffectiveStatus() {
        return status != null ? status : ProposalSlotStatus.PENDING;
    }
}
