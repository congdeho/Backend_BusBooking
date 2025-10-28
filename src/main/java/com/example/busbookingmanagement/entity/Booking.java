package com.example.busbookingmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ⚡ tránh lỗi ByteBuddyInterceptor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    // ================= USER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"bookings"}) // tránh vòng lặp khi serialize JSON
    private User user;

    // ================= SCHEDULE =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonIgnoreProperties({"bookings"})
    private Schedule schedule;

    // ================= PROMOTION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    @JsonIgnoreProperties({"bookings"})
    private Promotion promotion;

    // ================= BOOKING INFO =================
    @Column(nullable = false)
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    @Column(nullable = false)
    private Double discountAmount = 0.0;

    @Column(nullable = false)
    private Double finalAmount = 0.0;

    // ================= ENUM =================
    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}
