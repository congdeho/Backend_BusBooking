package com.example.busbookingmanagement.repository;

import com.example.busbookingmanagement.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.bus.busId = (SELECT sch.bus.busId FROM Schedule sch WHERE sch.scheduleId = :scheduleId) AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByScheduleId(Long scheduleId);
}
