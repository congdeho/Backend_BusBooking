package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.response.BookingResponse;
import com.example.busbookingmanagement.DTO.response.BookingDetailsResponse;
import com.example.busbookingmanagement.entity.Booking;

import java.util.List;

public interface BookingService {
    List<BookingResponse> getAll();
    BookingResponse getById(Long id);
    BookingResponse create(Booking booking);
    BookingResponse update(Long id, Booking booking);
    void delete(Long id);
    List<BookingResponse> getByUserId(Long userId);
    void selectSeats(Long bookingId, List<Long> seatIds);
    void cancelSeat(Long bookingId, Long seatId);

    // create a PENDING booking and persist chosen seat ids (BookingSeat entries)
    BookingResponse createPendingBookingWithSeats(Booking booking, List<Long> seatIds);

    // return full booking details (booking, seats, tickets, payment, invoice)
    BookingDetailsResponse getBookingDetails(Long bookingId);
}
