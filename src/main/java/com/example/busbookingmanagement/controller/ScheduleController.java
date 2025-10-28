package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.response.ScheduleResponse;
import com.example.busbookingmanagement.entity.Schedule;
import com.example.busbookingmanagement.service.ScheduleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule API", description = "Quản lý lịch trình")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public List<ScheduleResponse> getAll() {
        return scheduleService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getById(@PathVariable Long id) {
        return scheduleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Schedule create(@RequestBody Schedule schedule) {
        return scheduleService.create(schedule);
    }

    @PutMapping("/{id}")
    public Schedule update(@PathVariable Long id, @RequestBody Schedule schedule) {
        return scheduleService.update(id, schedule);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        scheduleService.delete(id);
    }
}
