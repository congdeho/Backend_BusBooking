package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.response.SeatDTO;
import com.example.busbookingmanagement.entity.Seat;
import com.example.busbookingmanagement.repository.SeatRepository;
import com.example.busbookingmanagement.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;

    @Override
    public List<SeatDTO> getAvailableSeatsBySchedule(Long scheduleId) {
        return seatRepository.findAvailableSeatsByScheduleId(scheduleId)
                .stream()
                .map(seat -> SeatDTO.builder()
                        .seatId(seat.getSeatId())
                        .seatNumber(seat.getSeatNumber())
                        .status(seat.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
