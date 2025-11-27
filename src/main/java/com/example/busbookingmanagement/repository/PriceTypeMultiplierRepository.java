package com.example.busbookingmanagement.repository;

import com.example.busbookingmanagement.entity.PriceTypeMultiplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceTypeMultiplierRepository extends JpaRepository<PriceTypeMultiplier, Long> {
    List<PriceTypeMultiplier> findByActiveTrueOrderByPriorityDesc();
}
