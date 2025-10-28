package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.request.DriverRequest;
import com.example.busbookingmanagement.DTO.response.DriverResponse;
import com.example.busbookingmanagement.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverController {

    private final DriverService driverService;

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAll() {
        return ResponseEntity.ok(driverService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(@RequestBody DriverRequest request) {
        return ResponseEntity.ok(driverService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> update(@PathVariable Long id, @RequestBody DriverRequest request) {
        return ResponseEntity.ok(driverService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
