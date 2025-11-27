package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.response.AuthResponse;
import com.example.busbookingmanagement.entity.Role;
import com.example.busbookingmanagement.entity.User;
import com.example.busbookingmanagement.repository.RoleRepository;
import com.example.busbookingmanagement.service.UserService;
import com.example.busbookingmanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class GoogleAuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.clientId:}")
    private String googleClientId;

    @PostMapping("/google")
    public ResponseEntity<?> googleSignIn(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing idToken"));
        }

        // Call Google's tokeninfo endpoint
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        ResponseEntity<Map<String, Object>> resp;
        try {
            resp = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
                    new ParameterizedTypeReference<>() {});
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        Map<String, Object> payload = resp.getBody();
        if (payload == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token payload"));
        }

        // Validate audience
        Object audObj = payload.get("aud");
        if (audObj == null || !audObj.toString().equals(googleClientId)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid audience"));
        }

        // Check email and verification
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String emailVerified = payload.get("email_verified") != null ? payload.get("email_verified").toString() : "false";

        if (email == null || !Boolean.parseBoolean(emailVerified)) {
            return ResponseEntity.status(401).body(Map.of("error", "Email not verified or missing"));
        }

        // Find or create user
        Optional<User> opt = userService.getAll().stream().filter(u -> email.equals(u.getEmail())).findFirst();
        User user;
        if (opt.isPresent()) {
            user = opt.get();
        } else {
            // find persisted CUSTOMER role or create it
            Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setRoleName("CUSTOMER");
                        r.setDescription("Default customer role");
                        return roleRepository.save(r);
                    });

            // create new user with CUSTOMER role
            user = new User();
            user.setEmail(email);
            user.setFullName(name != null ? name : "");
            user.setPhone("");
            // set a non-null random password (encoded) to satisfy DB not-null constraint
            String randomPwd = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(randomPwd));
            user.setRole(customerRole);
            user = userService.create(user);
        }

        // Generate JWT with subject = userId and claims
        Long userId = user.getUserId();
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER";
        String token = jwtUtil.generateToken(userId, user.getFullName(), user.getEmail(), roleName);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(roleName)
                .userId(userId)
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(Map.of("token", token, "user", authResponse));
    }
}
