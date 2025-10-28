package com.example.busbookingmanagement.DTO.request;

import lombok.Data;

@Data
public class DriverRequest {
    private String fullName;
    private String phone;
    private String licenseNumber;
    private String status; // ACTIVE / INACTIVE
}
