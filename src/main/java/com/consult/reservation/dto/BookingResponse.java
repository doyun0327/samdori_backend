package com.consult.reservation.dto;

import com.consult.reservation.entity.Booking;
import com.consult.reservation.entity.User;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BookingResponse {

    private final Long id;
    private final Long clientId;
    private final String clientName;
    private final Long counselorId;
    private final String counselorName;
    private final String date;
    private final String timeSlot;
    private final String status;
    private final LocalDateTime requestedAt;
    private final LocalDateTime respondedAt;
    private final LocalDateTime cancelledAt;

    public BookingResponse(Booking booking, User client, User counselor) {
        this.id = booking.getId();
        this.clientId = booking.getClientId();
        this.clientName = client.getName();
        this.counselorId = booking.getCounselorId();
        this.counselorName = counselor.getName();
        this.date = booking.getDate().toString();
        this.timeSlot = booking.getTimeSlot();
        this.status = booking.getStatus().name();
        this.requestedAt = booking.getRequestedAt();
        this.respondedAt = booking.getRespondedAt();
        this.cancelledAt = booking.getCancelledAt();
    }
}
