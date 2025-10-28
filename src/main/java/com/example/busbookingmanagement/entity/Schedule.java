package com.example.busbookingmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer availableSeats;

    @Enumerated(EnumType.STRING)
    private Status status = Status.SCHEDULED;

    public enum Status {
        SCHEDULED, DELAYED, CANCELLED, COMPLETED
    }
}
