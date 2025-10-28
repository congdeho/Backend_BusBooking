package com.example.busbookingmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public enum Role {
        ADMIN, STAFF, CUSTOMER
    }

    public enum Status {
        ACTIVE, INACTIVE
    }
}
