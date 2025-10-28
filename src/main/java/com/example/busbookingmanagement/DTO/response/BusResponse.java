package com.example.busbookingmanagement.DTO.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusResponse {
    private Long busId;
    private String licensePlate;
    private String busType;
    private Integer totalSeats;
    private String driverName;
    private String driverPhone;
    private String status;
}
