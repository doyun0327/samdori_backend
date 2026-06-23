package com.consult.reservation.repository;

import com.consult.reservation.entity.Booking;
import com.consult.reservation.entity.BookingStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByClientIdOrderByRequestedAtDesc(Long clientId);

    List<Booking> findByCounselorIdOrderByRequestedAtDesc(Long counselorId);

    boolean existsByCounselorIdAndDateAndTimeSlotAndStatusIn(
            Long counselorId, LocalDate date, String timeSlot, Collection<BookingStatus> statuses);
}
