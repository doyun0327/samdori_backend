package com.consult.reservation.repository;

import com.consult.reservation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /* 회원 가입 중복 체크 */
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    /* 로그인: loginId로 사용자 조회 */
    Optional<User> findByLoginId(String loginId);
}
