package com.example.busbookingmanagement.config;

import com.example.busbookingmanagement.util.JwtUtil;
import com.example.busbookingmanagement.entity.User;
import com.example.busbookingmanagement.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String path = req.getServletPath();

        // Only skip JWT processing for the public auth endpoints (login/register/google)
        if (path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/google") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/ws")) {
            chain.doFilter(req, res);
            return;
        }

        try {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                logger.debug("Authorization header present for path [{}], attempting JWT processing (token length={})", path, token.length());
                Long userId = null;
                String email = null;

                try {
                    userId = jwtUtil.extractUserId(token);
                    email = jwtUtil.extractEmail(token);
                } catch (Exception ex) {
                    logger.warn("Failed to extract claims from token: {}", ex.getMessage());
                }

                if ((userId != null || email != null) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = null;
                    if (email != null) {
                        user = userRepository.findByEmail(email).orElse(null);
                    }
                    if (user == null && userId != null) {
                        user = userRepository.findById(userId).orElse(null);
                    }

                    if (user != null) {
                        // validate token before trusting it
                        boolean valid = false;
                        try {
                            valid = jwtUtil.validateToken(token);
                        } catch (Exception ex) {
                            logger.warn("Token validation failed: {}", ex.getMessage());
                        }

                        if (valid) {
                            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "CUSTOMER";
                            // ensure authority format ROLE_{NAME}
                            String rolePrefixed = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

                            logger.debug("Setting authentication for user [{}], role=[{}], authorities=[{}, {}]", user.getEmail(), roleName, rolePrefixed, roleName);

                            UserDetails userDetails = org.springframework.security.core.userdetails.User
                                    .withUsername(user.getEmail())
                                    .password(user.getPassword())
                                    .authorities(rolePrefixed, roleName)
                                    .build();

                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            logger.debug("JWT authentication set for user: {}", user.getEmail());
                        } else {
                            // inside this branch `user` is known non-null (checked above), so log directly
                            logger.debug("JWT token invalid for user candidate: {}", user.getEmail());
                        }
                    } else {
                        logger.debug("No user found matching token claims (email/userId)");
                    }
                }
            } else {
                logger.debug("No Bearer Authorization header present for path [{}]", path);
            }
        } catch (Exception ex) {
            // Do not break filter chain -- log for debugging
            logger.error("Unexpected error in JwtAuthenticationFilter: {}", ex.getMessage(), ex);
        }

        // continue filter chain
        chain.doFilter(req, res);
    }
}