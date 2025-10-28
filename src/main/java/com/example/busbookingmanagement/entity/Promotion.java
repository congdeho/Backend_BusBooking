package com.example.busbookingmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promotionId;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // PERCENT or AMOUNT

    @Column(nullable = false)
    private Double discountValue;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public enum DiscountType {
        PERCENT, AMOUNT
    }

    public enum Status {
        ACTIVE, EXPIRED
    }
}
