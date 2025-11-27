package com.example.busbookingmanagement.DTO.response;

import com.example.busbookingmanagement.service.dto.PriceBreakdown;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponse {
    private Long scheduleId;
    private Long routeId;
    private Long busId;
    private String busType;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer availableSeats;
    private String status;

    // pricing
    private Integer basePrice;
    private Integer computedPrice;
    private PriceBreakdown priceBreakdown;
}

