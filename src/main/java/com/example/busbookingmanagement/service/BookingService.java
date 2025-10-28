package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.response.BookingResponse;
import com.example.busbookingmanagement.entity.Booking;

import java.util.List;

public interface BookingService {
    List<BookingResponse> getAll();
    BookingResponse getById(Long id);
    BookingResponse create(Booking booking);
    BookingResponse update(Long id, Booking booking);
    void delete(Long id);
    List<BookingResponse> getByUserId(Long userId);
}
