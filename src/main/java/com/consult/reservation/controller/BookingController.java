package com.consult.reservation.controller;

import com.consult.reservation.dto.BookingCancelRequest;
import com.consult.reservation.dto.BookingCounselorActionRequest;
import com.consult.reservation.dto.BookingCreateRequest;
import com.consult.reservation.dto.BookingResponse;
import com.consult.reservation.service.BookingService;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** POST /api/bookings — 예약 요청 생성 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody BookingCreateRequest request) {
        return bookingService.create(request);
    }

    /** GET /api/bookings/client?clientId= — 내담자 예약 목록 */
    @GetMapping("/client")
    public List<BookingResponse> getClientBookings(@RequestParam Long clientId) {
        return bookingService.findByClientId(clientId);
    }

    /** GET /api/bookings/counselor?counselorId= — 상담사 예약 요청 목록 */
    @GetMapping("/counselor")
    public List<BookingResponse> getCounselorBookings(@RequestParam Long counselorId) {
        return bookingService.findByCounselorId(counselorId);
    }

    /** PATCH /api/bookings/{bookingId}/accept — 예약 수락 */
    @PatchMapping("/{bookingId}/accept")
    public BookingResponse accept(
            @PathVariable Long bookingId,
            @RequestBody BookingCounselorActionRequest request) {
        return bookingService.accept(bookingId, request);
    }

    /** PATCH /api/bookings/{bookingId}/reject — 예약 거절 */
    @PatchMapping("/{bookingId}/reject")
    public BookingResponse reject(
            @PathVariable Long bookingId,
            @RequestBody BookingCounselorActionRequest request) {
        return bookingService.reject(bookingId, request);
    }

    /** PATCH /api/bookings/{bookingId}/cancel — 예약 취소 */
    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancel(
            @PathVariable Long bookingId,
            @RequestBody BookingCancelRequest request) {
        return bookingService.cancel(bookingId, request);
    }
}
