package com.consult.reservation.service;

import com.consult.reservation.dto.AvailabilityItemResponse;
import com.consult.reservation.dto.AvailabilityRequest;
import com.consult.reservation.dto.AvailabilityResponse;
import com.consult.reservation.entity.CounselorAvailability;
import com.consult.reservation.repository.CounselorAvailabilityRepository;
import com.consult.reservation.repository.UserRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CounselorAvailabilityService {

    private final CounselorAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    /**
     * 상담사·날짜·시간대가 모두 같을 때만 등록을 막고, 다른 시간대는 추가한다.
     * 예) 2026-06-20 + 09:00-10:00 중복 → skip / 2026-06-20 + 10:00-11:00 → insert
     */
    @Transactional
    public AvailabilityResponse save(AvailabilityRequest request) {
        validate(request);

        Long counselorId = request.getId();
        LocalDate date = parseDate(request.getDate());
        List<String> timeSlots = normalizeTimeSlots(request.getTimeSlots());

        Set<String> existingSlots = availabilityRepository
                .findByCounselorIdAndAvailabilityDateOrderByTimeSlotAsc(counselorId, date)
                .stream()
                .map(slot -> normalizeTimeSlot(slot.getTimeSlot()))
                .collect(Collectors.toSet());

        for (String timeSlot : timeSlots) {
            if (existingSlots.contains(timeSlot)) {
                continue;
            }

            CounselorAvailability availability = new CounselorAvailability();
            availability.setCounselorId(counselorId);
            availability.setAvailabilityDate(date);
            availability.setTimeSlot(timeSlot);
            availabilityRepository.save(availability);
            existingSlots.add(timeSlot);
        }

        List<String> savedSlots = findTimeSlotsByDate(counselorId, date);
        return new AvailabilityResponse(counselorId, date.toString(), savedSlots);
    }

    /** id·date·timeSlots에 해당하는 상담 가능 시간을 삭제한다. */
    @Transactional
    public AvailabilityResponse delete(AvailabilityRequest request) {
        validate(request);

        Long counselorId = request.getId();
        LocalDate date = parseDate(request.getDate());
        List<String> timeSlots = normalizeTimeSlots(request.getTimeSlots());

        List<CounselorAvailability> existing = availabilityRepository
                .findByCounselorIdAndAvailabilityDateOrderByTimeSlotAsc(counselorId, date);

        List<CounselorAvailability> targets = existing.stream()
                .filter(slot -> timeSlots.stream()
                        .anyMatch(requested -> isSameTimeSlot(slot.getTimeSlot(), requested)))
                .toList();

        if (targets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 상담 가능 시간이 없습니다.");
        }

        availabilityRepository.deleteAllInBatch(targets);

        List<String> deletedSlots = targets.stream()
                .map(CounselorAvailability::getTimeSlot)
                .toList();

        return new AvailabilityResponse(counselorId, date.toString(), deletedSlots);
    }

    /** 상담사 id로 등록된 상담 가능 시간 목록을 { date, timeSlot }[] 형태로 반환한다. */
    @Transactional(readOnly = true)
    public List<AvailabilityItemResponse> findAll(Long id) {
        validateCounselorId(id);

        return availabilityRepository.findByCounselorIdOrderByAvailabilityDateAscTimeSlotAsc(id).stream()
                .map(AvailabilityItemResponse::new)
                .toList();
    }

    private void validateCounselorId(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id를 입력해주세요.");
        }
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 상담사입니다.");
        }
    }

    private boolean isSameTimeSlot(String left, String right) {
        return normalizeTimeSlot(left).equals(normalizeTimeSlot(right));
    }

    private List<String> normalizeTimeSlots(List<String> timeSlots) {
        return timeSlots.stream()
                .map(this::normalizeTimeSlot)
                .distinct()
                .toList();
    }

    private String normalizeTimeSlot(String slot) {
        String[] parts = slot.trim().split("-");
        if (parts.length != 2) {
            return slot.trim();
        }
        return normalizeTime(parts[0]) + "-" + normalizeTime(parts[1]);
    }

    private String normalizeTime(String time) {
        String[] segments = time.trim().split(":");
        int hour = Integer.parseInt(segments[0]);
        int minute = Integer.parseInt(segments[1]);
        return String.format("%02d:%02d", hour, minute);
    }

    private List<String> findTimeSlotsByDate(Long counselorId, LocalDate date) {
        return availabilityRepository
                .findByCounselorIdAndAvailabilityDateOrderByTimeSlotAsc(counselorId, date)
                .stream()
                .map(CounselorAvailability::getTimeSlot)
                .toList();
    }

    private void validate(AvailabilityRequest request) {
        validateCounselorId(request.getId());
        if (request.getDate() == null || request.getDate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date를 입력해주세요.");
        }
        if (request.getTimeSlots() == null || request.getTimeSlots().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeSlots를 입력해주세요.");
        }
        if (request.getTimeSlots().stream().anyMatch(slot -> slot == null || slot.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeSlots에 빈 값이 있습니다.");
        }
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date 형식은 yyyy-MM-dd 입니다.");
        }
    }
}
