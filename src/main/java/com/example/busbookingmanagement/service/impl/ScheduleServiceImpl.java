package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.response.ScheduleResponse;
import com.example.busbookingmanagement.entity.Schedule;
import com.example.busbookingmanagement.repository.ScheduleRepository;
import com.example.busbookingmanagement.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private ScheduleResponse mapToDTO(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .startPoint(schedule.getRoute().getStartPoint())
                .endPoint(schedule.getRoute().getEndPoint())
                .distanceKm(schedule.getRoute().getDistanceKm())
                .basePrice(schedule.getRoute().getBasePrice())
                .busLicensePlate(schedule.getBus().getLicensePlate())
                .busType(schedule.getBus().getBusType())
                .availableSeats(schedule.getAvailableSeats())
                .departureTime(schedule.getDepartureTime().toString())
                .arrivalTime(schedule.getArrivalTime() != null ? schedule.getArrivalTime().toString() : null)
                .status(schedule.getStatus().name())
                .build();
    }

    @Override
    public List<ScheduleResponse> getAll() {
        return scheduleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ScheduleResponse> getById(Long id) {
        return scheduleRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public List<ScheduleResponse> getByRoute(Long routeId) {
        return scheduleRepository.findByRoute_RouteId(routeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Schedule create(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule update(Long id, Schedule schedule) {
        return scheduleRepository.findById(id)
                .map(s -> {
                    s.setDepartureTime(schedule.getDepartureTime());
                    s.setArrivalTime(schedule.getArrivalTime());
                    s.setAvailableSeats(schedule.getAvailableSeats());
                    s.setStatus(schedule.getStatus());
                    s.setBus(schedule.getBus());
                    s.setRoute(schedule.getRoute());
                    return scheduleRepository.save(s);
                })
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
    }

    @Override
    public void delete(Long id) {
        scheduleRepository.deleteById(id);
    }
}
