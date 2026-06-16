package com.consult.reservation.dto;

import com.consult.reservation.entity.User;
import lombok.Getter;

/**
 * 로그인 성공 시 클라이언트에 반환하는 최소 사용자 정보 DTO.
 * 상세 프로필은 별도 API에서 조회한다.
 */
@Getter
public class LoginResponse {

    private final Long id;
    private final String name;

    public LoginResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}
