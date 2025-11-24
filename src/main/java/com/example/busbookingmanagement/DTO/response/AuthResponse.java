package com.example.busbookingmanagement.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String fullName;
    private String phone; // added phone field
    private String role;
}
