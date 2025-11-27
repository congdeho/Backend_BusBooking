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

    // Feature flag: when false, websocket seat booking will NOT modify the database
    private static final boolean WEBSOCKET_DB_WRITE_ENABLED = false;

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

        if (WEBSOCKET_DB_WRITE_ENABLED) {
            // Original behavior: lock and update DB
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

            // NOTE: To ensure seats are only marked BOOKED after payment finalization,
            // do NOT modify seat.status or persist changes here. Instead return a simulated
            // response similar to the non-DB-write path.
            List<Seat> currentSeats = seatRepository.findAllSeatsByScheduleId(scheduleId);
            Map<Long, Seat> seatMap = currentSeats.stream()
                    .collect(Collectors.toMap(Seat::getSeatId, s -> s));

            Set<Long> requestedIds = new LinkedHashSet<>(seatIds);
            Set<Long> missingReq = requestedIds.stream()
                    .filter(id -> !seatMap.containsKey(id))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<Long> alreadyBookedReq = requestedIds.stream()
                    .filter(id -> seatMap.containsKey(id) && seatMap.get(id).getStatus() != Seat.Status.AVAILABLE)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            List<SeatDTO> dtoList = currentSeats.stream()
                    .map(s -> {
                        String status = s.getStatus().name();
                        if (requestedIds.contains(s.getSeatId()) && s.getStatus() == Seat.Status.AVAILABLE) {
                            status = Seat.Status.BOOKED.name(); // simulated only
                        }
                        return new SeatDTO(s.getSeatId(), s.getSeatNumber(), status);
                    })
                    .collect(Collectors.toList());

            SeatUpdateMessage update = new SeatUpdateMessage();
            update.setScheduleId(scheduleId);
            update.setSeats(dtoList);

            Set<Long> failed = new LinkedHashSet<>();
            failed.addAll(missingReq);
            failed.addAll(alreadyBookedReq);
            if (!failed.isEmpty()) {
                update.setFailedSeatIds(new ArrayList<>(failed));
            }
            log.info("WebSocket seat booking (DB write disabled for safety) for schedule {} requestedIds={} failed={}", scheduleId, seatIds, failed);
            return update;
        } else {
            // DB writes are disabled: simulate booking in response only (no entity changes, no save)
            List<Seat> currentSeats = seatRepository.findAllSeatsByScheduleId(scheduleId);
            Map<Long, Seat> seatMap = currentSeats.stream()
                    .collect(Collectors.toMap(Seat::getSeatId, s -> s));

            Set<Long> requested = new LinkedHashSet<>(seatIds);

            // missing: requested ids not present in seatMap
            Set<Long> missing = requested.stream()
                    .filter(id -> !seatMap.containsKey(id))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // alreadyBooked: requested ids that are not AVAILABLE
            Set<Long> alreadyBooked = requested.stream()
                    .filter(id -> seatMap.containsKey(id) && seatMap.get(id).getStatus() != Seat.Status.AVAILABLE)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // Build DTO list by marking requested & currently AVAILABLE seats as BOOKED (simulation only)
            List<SeatDTO> dtoList = currentSeats.stream()
                    .map(s -> {
                        String status = s.getStatus().name();
                        if (requested.contains(s.getSeatId()) && s.getStatus() == Seat.Status.AVAILABLE) {
                            status = Seat.Status.BOOKED.name(); // simulated
                        }
                        return new SeatDTO(s.getSeatId(), s.getSeatNumber(), status);
                    })
                    .collect(Collectors.toList());

            SeatUpdateMessage msg = new SeatUpdateMessage();
            msg.setScheduleId(scheduleId);
            msg.setSeats(dtoList);

            // Combine missing + alreadyBooked to failedSeatIds
            Set<Long> failed = new LinkedHashSet<>();
            failed.addAll(missing);
            failed.addAll(alreadyBooked);
            if (!failed.isEmpty()) {
                msg.setFailedSeatIds(new ArrayList<>(failed));
            }

            // Info log to indicate simulation mode
            log.info("WebSocket seat booking simulated (DB writes disabled) for schedule {} requestedIds={} failed={} ", scheduleId, seatIds, failed);

            return msg;
        }
    }
}
