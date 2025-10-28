package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.request.RouteRequest;
import com.example.busbookingmanagement.DTO.response.RouteResponse;
import java.util.List;

public interface RouteService {
    List<RouteResponse> getAllRoutes();
    RouteResponse getRouteById(Long id);
    RouteResponse createRoute(RouteRequest request);
    RouteResponse updateRoute(Long id, RouteRequest request);
    void deleteRoute(Long id);
}
