package com.consult.reservation.repository;

import com.consult.reservation.entity.CounselorAvailability;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselorAvailabilityRepository extends JpaRepository<CounselorAvailability, Long> {

    void deleteByCounselorIdAndAvailabilityDate(Long counselorId, LocalDate availabilityDate);

    List<CounselorAvailability> findByCounselorIdAndAvailabilityDateAndTimeSlotIn(
            Long counselorId, LocalDate availabilityDate, Collection<String> timeSlots);

    List<CounselorAvailability> findByCounselorIdOrderByAvailabilityDateAscTimeSlotAsc(Long counselorId);

    boolean existsByCounselorIdAndAvailabilityDateAndTimeSlot(
            Long counselorId, LocalDate availabilityDate, String timeSlot);
}
