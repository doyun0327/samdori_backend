package com.consult.reservation.repository;

import com.consult.reservation.entity.SlotProposal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SlotProposalRepository extends JpaRepository<SlotProposal, Long> {

    @Query("""
            SELECT sp, counselor, client
            FROM SlotProposal sp, User counselor, User client
            WHERE sp.counselorId = counselor.id
              AND sp.clientId = client.id
              AND sp.clientId = :clientId
            ORDER BY sp.createdAt DESC
            """)
    List<Object[]> findRowsByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @Query("""
            SELECT sp, counselor, client
            FROM SlotProposal sp, User counselor, User client
            WHERE sp.counselorId = counselor.id
              AND sp.clientId = client.id
              AND sp.counselorId = :counselorId
            ORDER BY sp.createdAt DESC
            """)
    List<Object[]> findRowsByCounselorId(@Param("counselorId") Long counselorId, Pageable pageable);
}
