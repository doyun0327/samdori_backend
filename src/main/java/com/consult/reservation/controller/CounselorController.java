package com.consult.reservation.controller;

import com.consult.reservation.dto.AvailabilityItemResponse;
import com.consult.reservation.dto.AvailabilityRequest;
import com.consult.reservation.dto.AvailabilityResponse;
import com.consult.reservation.service.CounselorAvailabilityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counselor")
@RequiredArgsConstructor
public class CounselorController {

    private final CounselorAvailabilityService availabilityService;

    /** GET /api/counselor/availability?id={id} — 상담 가능 시간 조회 */
    @GetMapping("/availability")
    public List<AvailabilityItemResponse> getAvailability(@RequestParam Long id) {
        return availabilityService.findAll(id);
    }

    /** POST /api/counselor/availability — 상담 가능 시간 등록 */
    @PostMapping("/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityResponse openAvailability(@RequestBody AvailabilityRequest request) {
        return availabilityService.save(request);
    }

    /** DELETE /api/counselor/availability — 상담 가능 시간 삭제 */
    @DeleteMapping("/availability")
    public AvailabilityResponse deleteAvailability(@RequestBody AvailabilityRequest request) {
        return availabilityService.delete(request);
    }
}
