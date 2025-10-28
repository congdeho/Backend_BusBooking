package com.example.busbookingmanagement.repository;

import com.example.busbookingmanagement.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByLicensePlate(String licensePlate);
}
