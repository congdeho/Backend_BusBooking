package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.response.BookingResponse;
import com.example.busbookingmanagement.entity.Booking;
import com.example.busbookingmanagement.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody Booking booking) {
        return ResponseEntity.ok(bookingService.create(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long id,
            @RequestBody Booking booking) {
        return ResponseEntity.ok(bookingService.update(id, booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
