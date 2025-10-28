package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.response.BusResponse;
import com.example.busbookingmanagement.entity.Bus;
import com.example.busbookingmanagement.service.BusService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
@Tag(name = "Bus API", description = "Quản lý thông tin xe khách")
public class BusController {

    private final BusService busService;

    @GetMapping
    public List<BusResponse> getAll() {
        return busService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> getById(@PathVariable Long id) {
        return busService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/plate/{licensePlate}")
    public ResponseEntity<BusResponse> getByLicensePlate(@PathVariable String licensePlate) {
        return busService.getByLicensePlate(licensePlate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Bus create(@RequestBody Bus bus) {
        return busService.create(bus);
    }

    @PutMapping("/{id}")
    public Bus update(@PathVariable Long id, @RequestBody Bus bus) {
        return busService.update(id, bus);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        busService.delete(id);
    }
}
