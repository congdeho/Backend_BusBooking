package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.websocket.BookedSeatsMessage;
import com.example.busbookingmanagement.DTO.websocket.HoldSeatMessage;
import com.example.busbookingmanagement.DTO.websocket.SeatUpdateMessage;
import com.example.busbookingmanagement.service.impl.SeatWebSocketServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SeatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(SeatWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SeatWebSocketServiceImpl seatService;

    @Autowired
    public SeatWebSocketController(SimpMessagingTemplate messagingTemplate, SeatWebSocketServiceImpl seatService) {
        this.messagingTemplate = messagingTemplate;
        this.seatService = seatService;
    }

    // Nhận giữ ghế từ client (for now simply returns current seat map)
    @MessageMapping("/hold-seat")
    public void holdSeat(HoldSeatMessage message) {
        log.info("Received hold-seat message: scheduleId={} ", message != null ? message.getScheduleId() : null);
        SeatUpdateMessage update = seatService.getSeatsForSchedule(message.getScheduleId());
        messagingTemplate.convertAndSend("/topic/seats/" + message.getScheduleId(), update);
    }

    // Nhận đặt ghế thành công từ client
    @MessageMapping("/booked-seats")
    public void bookedSeats(BookedSeatsMessage message) {
        log.info("Received booked-seats message: scheduleId={} seatIds={} ", message != null ? message.getScheduleId() : null, message != null ? message.getSeatIds() : null);
        SeatUpdateMessage update = seatService.bookSeats(message.getScheduleId(), message.getSeatIds());
        messagingTemplate.convertAndSend("/topic/seats/" + message.getScheduleId(), update);
    }
}
