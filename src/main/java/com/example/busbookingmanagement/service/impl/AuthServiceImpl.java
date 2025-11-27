package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.request.AuthRequest;
import com.example.busbookingmanagement.DTO.request.RegisterRequest;
import com.example.busbookingmanagement.DTO.response.AuthResponse;
import com.example.busbookingmanagement.util.JwtUtil;
import com.example.busbookingmanagement.entity.User;
import com.example.busbookingmanagement.entity.Role;
import com.example.busbookingmanagement.repository.UserRepository;
import com.example.busbookingmanagement.repository.RoleRepository;
import com.example.busbookingmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(customerRole)
                .build();
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUserId(), user.getFullName(), user.getEmail(), user.getRole().getRoleName());
        return new AuthResponse(token, user.getFullName(), user.getPhone(), user.getRole().getRoleName(), user.getUserId(), user.getEmail());
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("Password mismatch for user: " + request.getPassword());
            System.out.println("Password mismatch for user: " + user.getPassword());
            System.out.println(new BCryptPasswordEncoder().encode("123456"));
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getFullName(), user.getEmail(), user.getRole().getRoleName());
        return new AuthResponse(token, user.getFullName(), user.getPhone(), user.getRole().getRoleName(), user.getUserId(), user.getEmail());
    }
}
