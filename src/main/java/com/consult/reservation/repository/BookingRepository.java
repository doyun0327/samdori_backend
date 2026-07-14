package com.consult.reservation.repository;

import com.consult.reservation.entity.Booking;
import com.consult.reservation.entity.BookingStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b, client, counselor
            FROM Booking b, User client, User counselor
            WHERE b.clientId = client.id
              AND b.counselorId = counselor.id
              AND b.clientId = :clientId
            ORDER BY b.requestedAt DESC
            """)
    List<Object[]> findRowsByClientId(@Param("clientId") Long clientId, Pageable pageable);

    @Query("""
            SELECT b, client, counselor
            FROM Booking b, User client, User counselor
            WHERE b.clientId = client.id
              AND b.counselorId = counselor.id
              AND b.counselorId = :counselorId
            ORDER BY b.requestedAt DESC
            """)
    List<Object[]> findRowsByCounselorId(@Param("counselorId") Long counselorId, Pageable pageable);

    boolean existsByCounselorIdAndDateAndTimeSlotAndStatusIn(
            Long counselorId, LocalDate date, String timeSlot, Collection<BookingStatus> statuses);
}
