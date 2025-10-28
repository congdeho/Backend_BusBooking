package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.request.RouteRequest;
import com.example.busbookingmanagement.DTO.response.RouteResponse;
import com.example.busbookingmanagement.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "Lấy danh sách tất cả tuyến xe")
    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @Operation(summary = "Lấy thông tin tuyến xe theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @Operation(summary = "Tạo mới tuyến xe")
    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.createRoute(request));
    }

    @Operation(summary = "Cập nhật tuyến xe theo ID")
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id, @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @Operation(summary = "Xóa tuyến xe theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
