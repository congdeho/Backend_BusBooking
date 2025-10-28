package com.example.busbookingmanagement.repository;

import com.example.busbookingmanagement.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByRoute_RouteId(Long routeId);

    List<Schedule> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);
}
