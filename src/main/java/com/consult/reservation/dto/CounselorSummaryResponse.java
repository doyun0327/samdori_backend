package com.consult.reservation.dto;

import com.consult.reservation.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 상담사 목록 조회용 DTO */
@Getter
@RequiredArgsConstructor
public class CounselorSummaryResponse {

    private final Long id;
    private final String name;
    private final String centerName;

    public CounselorSummaryResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.centerName = user.getCenterName();
    }
}
