package com.example.busbookingmanagement.DTO.websocket;

public class HoldSeatMessage {
    private Long scheduleId;
    private Long seatId;

    public HoldSeatMessage() {}

    public HoldSeatMessage(Long scheduleId, Long seatId) {
        this.scheduleId = scheduleId;
        this.seatId = seatId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }
}

