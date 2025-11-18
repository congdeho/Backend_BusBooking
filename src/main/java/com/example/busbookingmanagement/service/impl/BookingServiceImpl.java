package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.response.BookingResponse;
import com.example.busbookingmanagement.entity.Booking;
import com.example.busbookingmanagement.entity.Seat;
import com.example.busbookingmanagement.repository.BookingRepository;
import com.example.busbookingmanagement.repository.SeatRepository;
import com.example.busbookingmanagement.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    @Override
    public List<BookingResponse> getAll() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return mapToDTO(booking);
    }

    @Override
    public BookingResponse create(Booking booking) {
        Booking saved = bookingRepository.save(booking);
        return mapToDTO(saved);
    }

    @Override
    public BookingResponse update(Long id, Booking booking) {
        Booking updated = bookingRepository.findById(id)
                .map(b -> {
                    b.setStatus(booking.getStatus());
                    b.setFinalAmount(booking.getFinalAmount());
                    b.setTotalAmount(booking.getTotalAmount());
                    b.setDiscountAmount(booking.getDiscountAmount());
                    return bookingRepository.save(b);
                })
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return mapToDTO(updated);
    }

    @Override
    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingResponse> getByUserId(Long userId) {
        return bookingRepository.findAll()
                .stream()
                .filter(b -> b.getUser() != null && userId.equals(b.getUser().getUserId()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void selectSeats(Long bookingId, List<Long> seatIds) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        List<Seat> seats = seatRepository.findAllById(seatIds);
        for (Seat seat : seats) {
            if (seat.getStatus() != Seat.Status.AVAILABLE) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
            }
            seat.setStatus(Seat.Status.BOOKED);
            seatRepository.save(seat);
            // TODO: Tạo ticket cho booking-seat nếu cần
        }
    }

    @Override
    @Transactional
    public void cancelSeat(Long bookingId, Long seatId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);
        // TODO: Xóa ticket booking-seat nếu cần
    }

    // Helper method: convert entity → DTO
    private BookingResponse mapToDTO(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userName(booking.getUser() != null ? booking.getUser().getFullName() : null)
                .route(booking.getSchedule() != null
                        ? booking.getSchedule().getRoute().getStartPoint() + " - " +
                        booking.getSchedule().getRoute().getEndPoint()
                        : null)
                .bookingDate(booking.getBookingDate())
                .finalAmount(booking.getFinalAmount())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .build();
    }
}
