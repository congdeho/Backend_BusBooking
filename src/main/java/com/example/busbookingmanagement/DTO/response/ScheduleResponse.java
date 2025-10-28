package com.example.busbookingmanagement.DTO.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long scheduleId;
    private String startPoint;
    private String endPoint;
    private Double distanceKm;
    private Double basePrice;

    private String busLicensePlate;
    private String busType;
    private Integer availableSeats;

    private String departureTime;
    private String arrivalTime;
    private String status;
}
