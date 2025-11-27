package com.example.busbookingmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // pick up the CorsConfigurationSource bean
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                // use stateless session for JWT
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // allow preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // allow websocket/sockjs endpoints (handshake)
                        .requestMatchers("/ws/**", "/ws/seats/**").permitAll()
                        // public auth endpoints explicitly
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/google"
                        ).permitAll()
                        // open API docs / swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // public read endpoints (GET only)
                        .requestMatchers(HttpMethod.GET,
                                "/api/routes/**",
                                "/api/buses/**",
                                "/api/schedules/**",
                                "/api/seats/**",
                                "/api/promotions/**",
                                "/api/notifications/**",
                                "/api/bookings/**"
                        ).permitAll()
                        // allow frontend to add seats to booking in dev (no auth)
                        .requestMatchers(HttpMethod.POST, "/api/bookings/*/seats").permitAll()
                        // require authentication for creating/modifying bookings and payments
                        .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").authenticated()

                        // ensure VNPay confirm endpoints are public BEFORE the general payments rule
                        .requestMatchers(HttpMethod.POST, "/api/payments/vnpay/confirm", "/api/payment/vnpay/confirm").permitAll()

                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/payment/vnpay/confirm").permitAll()
                        .requestMatchers("/api/payments/vnpay/confirm").permitAll() // keep confirm public if VNPay calls it
                        // admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // adjust as needed
                        // everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}