package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.response.SeatDTO;
import java.util.List;

public interface SeatService {
    List<SeatDTO> getAvailableSeatsBySchedule(Long scheduleId);
}
