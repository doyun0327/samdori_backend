package com.consult.reservation.repository;

import com.consult.reservation.entity.SlotProposal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotProposalRepository extends JpaRepository<SlotProposal, Long> {

    List<SlotProposal> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<SlotProposal> findByCounselorIdOrderByCreatedAtDesc(Long counselorId);
}
