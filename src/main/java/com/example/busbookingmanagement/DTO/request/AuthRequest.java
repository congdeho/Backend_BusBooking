package com.example.busbookingmanagement.DTO.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
