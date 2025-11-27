package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.request.RouteRequest;
import com.example.busbookingmanagement.DTO.response.RouteResponse;
import com.example.busbookingmanagement.entity.Route;
import com.example.busbookingmanagement.repository.RouteRepository;
import com.example.busbookingmanagement.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    @Override
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));
        return toResponse(route);
    }

    @Override
    public RouteResponse createRoute(RouteRequest request) {
        Route route = Route.builder()
                .startPoint(request.getStartPoint())
                .endPoint(request.getEndPoint())
                .distanceKm(request.getDistanceKm())
                .basePrice(request.getBasePrice())
                .estimatedTime(request.getEstimatedTime())
                .build();
        return toResponse(routeRepository.save(route));
    }

    @Override
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with id: " + id));

        route.setStartPoint(request.getStartPoint());
        route.setEndPoint(request.getEndPoint());
        route.setDistanceKm(request.getDistanceKm());
        route.setBasePrice(request.getBasePrice());
        route.setEstimatedTime(request.getEstimatedTime());

        return toResponse(routeRepository.save(route));
    }

    @Override
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    @Override
    public List<RouteResponse> findByStartAndEnd(String startPoint, String endPoint) {
        return routeRepository
                .findByStartPointContainingIgnoreCaseAndEndPointContainingIgnoreCase(
                        startPoint == null ? "" : startPoint,
                        endPoint == null ? "" : endPoint)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RouteResponse toResponse(Route route) {
        return RouteResponse.builder()
                .routeId(route.getRouteId())
                .startPoint(route.getStartPoint())
                .endPoint(route.getEndPoint())
                .distanceKm(route.getDistanceKm())
                .basePrice(route.getBasePrice())
                .estimatedTime(route.getEstimatedTime())
                .build();
    }
}
