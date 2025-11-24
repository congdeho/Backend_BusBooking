package com.example.busbookingmanagement.repository;

import com.example.busbookingmanagement.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.bus.busId = (SELECT sch.bus.busId FROM Schedule sch WHERE sch.scheduleId = :scheduleId) AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByScheduleId(Long scheduleId);

    // Return all seats for the bus used by the given schedule (no status filter)
    @Query("SELECT s FROM Seat s WHERE s.bus.busId = (SELECT sch.bus.busId FROM Schedule sch WHERE sch.scheduleId = :scheduleId)")
    List<Seat> findAllSeatsByScheduleId(Long scheduleId);

    // Select seats by ids that belong to the schedule's bus with a PESSIMISTIC_WRITE lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.seatId IN :ids AND s.bus.busId = (SELECT sch.bus.busId FROM Schedule sch WHERE sch.scheduleId = :scheduleId)")
    List<Seat> findSeatsForUpdateByIdsAndScheduleId(@Param("ids") List<Long> ids, @Param("scheduleId") Long scheduleId);
}
