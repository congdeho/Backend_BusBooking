package com.example.busbookingmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "price_type_multipliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceTypeMultiplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_regex", nullable = false)
    private String matchRegex;

    @Column(nullable = false)
    private double multiplier;

    private String description;

    private boolean active = true;

    private int priority = 0;
}

