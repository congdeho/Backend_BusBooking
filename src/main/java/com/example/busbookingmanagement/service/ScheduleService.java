package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.response.ScheduleResponse;
import com.example.busbookingmanagement.entity.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    List<ScheduleResponse> getAll();
    Optional<ScheduleResponse> getById(Long id);
    List<ScheduleResponse> getByRoute(Long routeId);
    // Get schedules for a route on a specific date
    List<ScheduleResponse> getByRouteAndDate(Long routeId, LocalDate date);
    Schedule create(Schedule schedule);
    Schedule update(Long id, Schedule schedule);
    void delete(Long id);
}
