package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.response.BookingResponse;
import com.example.busbookingmanagement.entity.Booking;
import com.example.busbookingmanagement.repository.BookingRepository;
import com.example.busbookingmanagement.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

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
