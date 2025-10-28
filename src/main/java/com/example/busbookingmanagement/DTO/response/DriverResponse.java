package com.example.busbookingmanagement.DTO.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DriverResponse {
    private Long driverId;
    private String fullName;
    private String phone;
    private String licenseNumber;
    private String status;
    private LocalDateTime createdAt;
}

