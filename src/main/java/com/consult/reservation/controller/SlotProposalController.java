package com.consult.reservation.controller;

import com.consult.reservation.dto.CountResponse;
import com.consult.reservation.dto.SlotProposalBookRequest;
import com.consult.reservation.dto.SlotProposalBookResponse;
import com.consult.reservation.dto.SlotProposalCancelRequest;
import com.consult.reservation.dto.SlotProposalDeclineRequest;
import com.consult.reservation.dto.SlotProposalCreateRequest;
import com.consult.reservation.dto.SlotProposalResponse;
import com.consult.reservation.service.SlotProposalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slot-proposals")
@RequiredArgsConstructor
public class SlotProposalController {

    private final SlotProposalService slotProposalService;

    /** POST /api/slot-proposals — 상담사 → 고객 시간 제안 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotProposalResponse create(@RequestBody SlotProposalCreateRequest request) {
        return slotProposalService.create(request);
    }

    /** GET /api/slot-proposals/client/count?clientId= — 고객 제안 건수 */
    @GetMapping("/client/count")
    public CountResponse countClientProposals(@RequestParam Long clientId) {
        return slotProposalService.countByClientId(clientId);
    }

    /** GET /api/slot-proposals/client?clientId= — 고객 제안 목록 (버튼 클릭 시) */
    @GetMapping("/client")
    public List<SlotProposalResponse> getClientProposals(@RequestParam Long clientId) {
        return slotProposalService.findByClientId(clientId);
    }

    /** GET /api/slot-proposals/counselor/count?counselorId= — 상담사 제안 건수 */
    @GetMapping("/counselor/count")
    public CountResponse countCounselorProposals(@RequestParam Long counselorId) {
        return slotProposalService.countByCounselorId(counselorId);
    }

    /** GET /api/slot-proposals/counselor?counselorId= — 상담사 제안 목록 (버튼 클릭 시) */
    @GetMapping("/counselor")
    public List<SlotProposalResponse> getCounselorProposals(@RequestParam Long counselorId) {
        return slotProposalService.findByCounselorId(counselorId);
    }

    /** POST /api/slot-proposals/{proposalId}/book — 고객 슬롯 선택 → 예약 */
    @PostMapping("/{proposalId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotProposalBookResponse book(
            @PathVariable Long proposalId,
            @RequestBody SlotProposalBookRequest request) {
        return slotProposalService.book(proposalId, request);
    }

    /** PATCH /api/slot-proposals/{proposalId}/decline — 고객 슬롯 거절 */
    @PatchMapping("/{proposalId}/decline")
    public SlotProposalResponse decline(
            @PathVariable Long proposalId,
            @RequestBody SlotProposalDeclineRequest request) {
        return slotProposalService.decline(proposalId, request);
    }

    /** PATCH /api/slot-proposals/{proposalId}/cancel — 상담사 제안 취소 */
    @PatchMapping("/{proposalId}/cancel")
    public SlotProposalResponse cancel(
            @PathVariable Long proposalId,
            @RequestBody SlotProposalCancelRequest request) {
        return slotProposalService.cancel(proposalId, request);
    }
}
