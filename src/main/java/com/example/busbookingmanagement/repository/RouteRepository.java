package com.example.busbookingmanagement.repository;

import com.example.busbookingmanagement.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // Find routes by start and end points (case-insensitive, partial match)
    List<Route> findByStartPointContainingIgnoreCaseAndEndPointContainingIgnoreCase(String startPoint, String endPoint);
}
