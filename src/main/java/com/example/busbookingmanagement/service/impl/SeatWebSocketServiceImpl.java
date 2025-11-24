package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.websocket.SeatDTO;
import com.example.busbookingmanagement.DTO.websocket.SeatUpdateMessage;
import com.example.busbookingmanagement.entity.Seat;
import com.example.busbookingmanagement.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatWebSocketServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(SeatWebSocketServiceImpl.class);

    private final SeatRepository seatRepository;

    @Autowired
    public SeatWebSocketServiceImpl(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    // Return full seat map (all statuses) for schedule
    public SeatUpdateMessage getSeatsForSchedule(Long scheduleId) {
        List<Seat> seats = seatRepository.findAllSeatsByScheduleId(scheduleId);
        List<SeatDTO> dtoList = seats.stream()
                .map(s -> new SeatDTO(s.getSeatId(), s.getSeatNumber(), s.getStatus().name()))
                .collect(Collectors.toList());
        SeatUpdateMessage msg = new SeatUpdateMessage();
        msg.setScheduleId(scheduleId);
        msg.setSeats(dtoList);
        return msg;
    }

    // Booking seats: transactional update with pessimistic lock to avoid double-booking
    @Transactional
    public SeatUpdateMessage bookSeats(Long scheduleId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return getSeatsForSchedule(scheduleId);
        }

        // Lock and fetch the seats that belong to this schedule's bus
        List<Seat> toCheck = seatRepository.findSeatsForUpdateByIdsAndScheduleId(seatIds, scheduleId);
        Set<Long> requested = new HashSet<>(seatIds);

        // Determine which ids were not found (invalid or not part of schedule)
        Set<Long> foundIds = toCheck.stream().map(Seat::getSeatId).collect(Collectors.toSet());
        Set<Long> missing = requested.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            log.warn("Requested seatIds not found or not part of schedule {}: {}", scheduleId, missing);
        }

        // Only book seats that are currently AVAILABLE
        List<Seat> toBook = toCheck.stream()
                .filter(s -> s.getStatus() == Seat.Status.AVAILABLE)
                .collect(Collectors.toList());

        List<Long> alreadyBooked = toCheck.stream()
                .filter(s -> s.getStatus() != Seat.Status.AVAILABLE)
                .map(Seat::getSeatId)
                .collect(Collectors.toList());

        if (!alreadyBooked.isEmpty()) {
            log.info("Some seats already booked/maintenance for schedule {}: {}", scheduleId, alreadyBooked);
        }

        for (Seat s : toBook) {
            s.setStatus(Seat.Status.BOOKED);
        }

        seatRepository.saveAll(toBook);

        // Return updated full seat map
        SeatUpdateMessage update = getSeatsForSchedule(scheduleId);
        // Combine missing + alreadyBooked to failedSeatIds
        Set<Long> failed = new LinkedHashSet<>();
        failed.addAll(missing);
        failed.addAll(alreadyBooked);
        if (!failed.isEmpty()) {
            update.setFailedSeatIds(new ArrayList<>(failed));
        }
        return update;
    }
}
