package com.consult.reservation.dto;

import com.consult.reservation.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 내담자 검색 결과 DTO */
@Getter
@RequiredArgsConstructor
public class ClientSummaryResponse {

    private final Long id;
    private final String name;
    private final String phoneNumber;

    public ClientSummaryResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
    }
}
