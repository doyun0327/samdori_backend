package com.consult.reservation.dto;

import com.consult.reservation.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {

    private final Long id;
    private final String loginId;
    private final String name;
    private final String phoneNumber;
    private final String email;

    public UserResponse(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.email = user.getEmail();
    }
}
