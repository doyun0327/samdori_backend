package com.consult.reservation.dto;

import com.consult.reservation.entity.CounselorAvailability;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** GET /api/counselor/availability 조회 항목 DTO */
@Getter
@RequiredArgsConstructor
public class AvailabilityItemResponse {

    private final String date;
    private final String timeSlot;

    public AvailabilityItemResponse(CounselorAvailability availability) {
        this.date = availability.getAvailabilityDate().toString();
        this.timeSlot = availability.getTimeSlot();
    }
}
