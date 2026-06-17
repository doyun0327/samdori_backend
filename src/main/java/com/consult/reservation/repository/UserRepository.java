package com.consult.reservation.repository;

import com.consult.reservation.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /* 회원 가입 중복 체크 */
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    /* 로그인: loginId로 사용자 조회 */
    Optional<User> findByLoginId(String loginId);

    /* 상담사 목록 조회 */
    List<User> findByRoleOrderByNameAsc(String role);
}
