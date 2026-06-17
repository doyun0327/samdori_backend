package com.consult.reservation.service;

import com.consult.reservation.dto.AvailabilityRequest;
import com.consult.reservation.dto.AvailabilityResponse;
import com.consult.reservation.entity.CounselorAvailability;
import com.consult.reservation.repository.CounselorAvailabilityRepository;
import com.consult.reservation.repository.UserRepository;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
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
     * 해당 상담사·날짜의 기존 슬롯을 지우고, 요청받은 timeSlots로 다시 등록한다.
     */
    @Transactional
    public AvailabilityResponse save(AvailabilityRequest request) {
        validate(request);

        Long counselorId = request.getId();
        LocalDate date = parseDate(request.getDate());
        List<String> timeSlots = normalizeTimeSlots(request.getTimeSlots());

        availabilityRepository.deleteByCounselorIdAndAvailabilityDate(counselorId, date);

        List<CounselorAvailability> entities = timeSlots.stream()
                .map(timeSlot -> {
                    CounselorAvailability availability = new CounselorAvailability();
                    availability.setCounselorId(counselorId);
                    availability.setAvailabilityDate(date);
                    availability.setTimeSlot(timeSlot);
                    return availability;
                })
                .toList();

        availabilityRepository.saveAll(entities);

        return new AvailabilityResponse(counselorId, date.toString(), timeSlots);
    }

    /** id·date·timeSlots에 해당하는 상담 가능 시간을 삭제한다. */
    @Transactional
    public AvailabilityResponse delete(AvailabilityRequest request) {
        validate(request);

        Long counselorId = request.getId();
        LocalDate date = parseDate(request.getDate());
        List<String> timeSlots = normalizeTimeSlots(request.getTimeSlots());

        List<CounselorAvailability> targets = availabilityRepository
                .findByCounselorIdAndAvailabilityDateAndTimeSlotIn(counselorId, date, timeSlots);

        if (targets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 상담 가능 시간이 없습니다.");
        }

        availabilityRepository.deleteAllInBatch(targets);

        List<String> deletedSlots = targets.stream()
                .map(CounselorAvailability::getTimeSlot)
                .toList();

        return new AvailabilityResponse(counselorId, date.toString(), deletedSlots);
    }

    private List<String> normalizeTimeSlots(List<String> timeSlots) {
        return timeSlots.stream()
                .map(String::trim)
                .distinct()
                .toList();
    }

    private void validate(AvailabilityRequest request) {
        if (request.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id를 입력해주세요.");
        }
        if (!userRepository.existsById(request.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 상담사입니다.");
        }
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
