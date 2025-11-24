package com.example.busbookingmanagement.DTO.websocket;

import java.util.List;

public class SeatUpdateMessage {
    private Long scheduleId;
    private List<SeatDTO> seats;
    private List<Long> failedSeatIds; // seat ids that failed to book (already booked/missing)

    public SeatUpdateMessage() {}

    public SeatUpdateMessage(Long scheduleId, List<SeatDTO> seats) {
        this.scheduleId = scheduleId;
        this.seats = seats;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<SeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDTO> seats) {
        this.seats = seats;
    }

    public List<Long> getFailedSeatIds() {
        return failedSeatIds;
    }

    public void setFailedSeatIds(List<Long> failedSeatIds) {
        this.failedSeatIds = failedSeatIds;
    }
}
