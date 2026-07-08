package com.consult.reservation.repository;

import com.consult.reservation.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    /* 회원 가입 중복 체크 */
    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    /* 로그인: loginId로 사용자 조회 */
    Optional<User> findByLoginId(String loginId);

    /* 상담사 목록 조회 */
    List<User> findByRoleOrderByNameAsc(String role);

    /* 내담자 검색: 이름 포함 OR 전화번호 포함 */
    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
              AND (
                LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')
              )
            ORDER BY u.name ASC
            """)
    List<User> searchClientsByKeyword(
            @Param("role") String role,
            @Param("keyword") String keyword,
            Pageable pageable);
}
