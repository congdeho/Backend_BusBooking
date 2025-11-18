package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings/{bookingId}/seats")
@RequiredArgsConstructor
public class BookingSeatController {
    private final BookingService bookingService;

    // POST /api/bookings/{bookingId}/seats – Chọn ghế khi đặt vé
    @PostMapping
    public ResponseEntity<?> selectSeats(@PathVariable Long bookingId, @RequestBody Map<String, List<Long>> body) {
        List<Long> seatIds = body.get("seatIds");
        bookingService.selectSeats(bookingId, seatIds);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/bookings/{bookingId}/seats/{seatId} – Hủy ghế khi đặt vé
    @DeleteMapping("/{seatId}")
    public ResponseEntity<?> cancelSeat(@PathVariable Long bookingId, @PathVariable Long seatId) {
        bookingService.cancelSeat(bookingId, seatId);
        return ResponseEntity.ok().build();
    }
}

