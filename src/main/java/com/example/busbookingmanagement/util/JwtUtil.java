package com.example.busbookingmanagement.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.security.Key;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    // constructor injection of secret and expiration
    public JwtUtil(@Value("${app.jwt.secret:busbooking_secret_busbooking_secret_1234}") String secret,
                   @Value("${app.jwt.expirationMs:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    // New generateToken: subject = userId, include claims name/email/role
    public String generateToken(Long userId, String fullName, String email, String role) {
        return Jwts.builder()
                .setSubject(userId != null ? userId.toString() : null)
                .claim("name", fullName)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Backwards-compatible overload: keep a method that accepts email (used by some existing code)
    public String generateToken(String email) {
        // fallback: set subject to email
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        String sub = claims.getSubject();
        if (sub == null) return null;
        try {
            return Long.parseLong(sub);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        Claims claims = parseClaims(token);
        Object email = claims.get("email");
        return email != null ? email.toString() : null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
