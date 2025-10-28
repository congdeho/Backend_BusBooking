package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.request.AuthRequest;
import com.example.busbookingmanagement.DTO.response.AuthResponse;
import com.example.busbookingmanagement.DTO.request.RegisterRequest;
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}

