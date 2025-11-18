package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.service.SeatService;
import com.example.busbookingmanagement.entity.Seat;
import com.example.busbookingmanagement.DTO.response.SeatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {
    private final SeatService seatService;

    // GET /api/seats/schedule/{scheduleId} – Lấy danh sách ghế trống
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<SeatDTO>> getAvailableSeats(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(seatService.getAvailableSeatsBySchedule(scheduleId));
    }
}
