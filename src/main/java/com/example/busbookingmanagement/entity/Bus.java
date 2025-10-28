package com.example.busbookingmanagement.entity;

import com.example.busbookingmanagement.entity.enums.BusStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bus_id")
    private Long busId;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Column(name = "bus_type")
    private String busType;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusStatus status;
}

