package com.consult.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequest {

    private String loginId;
    private String name;
    private String phoneNumber;
    private String email;
    private String password;
}
