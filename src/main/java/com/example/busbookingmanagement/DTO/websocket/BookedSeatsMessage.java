package com.example.busbookingmanagement.DTO.websocket;

import java.util.List;

public class BookedSeatsMessage {
    private Long scheduleId;
    private List<Long> seatIds;

    public BookedSeatsMessage() {}

    public BookedSeatsMessage(Long scheduleId, List<Long> seatIds) {
        this.scheduleId = scheduleId;
        this.seatIds = seatIds;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
    }
}

