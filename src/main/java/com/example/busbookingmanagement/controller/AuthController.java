package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.request.AuthRequest;
import com.example.busbookingmanagement.DTO.request.RegisterRequest;
import com.example.busbookingmanagement.DTO.request.UpdateProfileRequest;
import com.example.busbookingmanagement.DTO.response.AuthResponse;
import com.example.busbookingmanagement.entity.User;
import com.example.busbookingmanagement.service.AuthService;
import com.example.busbookingmanagement.service.UserService;
import com.example.busbookingmanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        String email = auth.getName();
        User user = userService.getAll().stream().filter(u -> email.equals(u.getEmail())).findFirst().orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phone", user.getPhone(),
                "role", roleName
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        String email = auth.getName();
        User user = userService.getAll().stream().filter(u -> email.equals(u.getEmail())).findFirst().orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));

        User toUpdate = new User();
        toUpdate.setFullName(req.getFullName() != null ? req.getFullName() : user.getFullName());
        toUpdate.setPhone(req.getPhone() != null ? req.getPhone() : user.getPhone());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            toUpdate.setPassword(req.getPassword());
        }

        User updated = userService.update(user.getUserId(), toUpdate);
        return ResponseEntity.ok(Map.of(
                "userId", updated.getUserId(),
                "fullName", updated.getFullName(),
                "email", updated.getEmail(),
                "phone", updated.getPhone(),
                "role", updated.getRole() != null ? updated.getRole().getRoleName() : null
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        String email = auth.getName();
        User user = userService.getAll().stream().filter(u -> email.equals(u.getEmail())).findFirst().orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        String token = jwtUtil.generateToken(user.getUserId(), user.getFullName(), user.getEmail(), user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER");
        return ResponseEntity.ok(Map.of("token", token));
    }
}
