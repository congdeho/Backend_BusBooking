package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.response.ScheduleResponse;
import com.example.busbookingmanagement.entity.Schedule;
import com.example.busbookingmanagement.repository.ScheduleRepository;
import com.example.busbookingmanagement.service.PriceCalculationService;
import com.example.busbookingmanagement.service.ScheduleService;
import com.example.busbookingmanagement.service.dto.PriceCalculationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final PriceCalculationService priceCalculationService;

    @Override
    public List<ScheduleResponse> getAll() {
        List<Schedule> schedules = scheduleRepository.findAll();
        return schedules.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Optional<ScheduleResponse> getById(Long id) {
        return scheduleRepository.findById(id).map(this::toResponse);
    }

    @Override
    public List<ScheduleResponse> getByRoute(Long routeId) {
        List<Schedule> schedules = scheduleRepository.findByRoute_RouteId(routeId);
        return schedules.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ScheduleResponse> getByRouteAndDate(Long routeId, LocalDate date) {
        if (routeId == null || date == null) {
            return List.of();
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Schedule> schedules = scheduleRepository.findByRoute_RouteIdAndDepartureTimeBetween(routeId, startOfDay, endOfDay);
        return schedules.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public Schedule create(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule update(Long id, Schedule schedule) {
        schedule.setScheduleId(id);
        return scheduleRepository.save(schedule);
    }

    @Override
    public void delete(Long id) {
        scheduleRepository.deleteById(id);
    }

    private ScheduleResponse toResponse(Schedule s) {
        PriceCalculationResult calc = priceCalculationService.calculatePrice(
                (s.getRoute() != null && s.getRoute().getBasePrice() != null) ? s.getRoute().getBasePrice().intValue() : null,
                null,
                (s.getBus() != null) ? s.getBus().getBusType() : null,
                null
        );

        ScheduleResponse r = ScheduleResponse.builder()
                .scheduleId(s.getScheduleId())
                .routeId(s.getRoute() != null ? s.getRoute().getRouteId() : null)
                .busId(s.getBus() != null ? s.getBus().getBusId() : null)
                .busType(s.getBus() != null ? s.getBus().getBusType() : null)
                .departureTime(s.getDepartureTime())
                .arrivalTime(s.getArrivalTime())
                .availableSeats(s.getAvailableSeats())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .basePrice(s.getRoute() != null && s.getRoute().getBasePrice() != null ? s.getRoute().getBasePrice().intValue() : null)
                .computedPrice(calc != null ? calc.getComputedPrice() : null)
                .priceBreakdown(calc != null ? calc.getPriceBreakdown() : null)
                .build();
        return r;
    }
}
