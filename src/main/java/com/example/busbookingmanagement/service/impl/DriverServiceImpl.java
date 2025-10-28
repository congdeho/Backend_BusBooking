package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.request.DriverRequest;
import com.example.busbookingmanagement.DTO.response.DriverResponse;
import com.example.busbookingmanagement.entity.Driver;
import com.example.busbookingmanagement.repository.DriverRepository;
import com.example.busbookingmanagement.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    @Override
    public List<DriverResponse> getAll() {
        return driverRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DriverResponse getById(Long id) {
        return driverRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
    }

    @Override
    public DriverResponse create(DriverRequest request) {
        Driver driver = Driver.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .licenseNumber(request.getLicenseNumber())
                .status(Driver.Status.valueOf(request.getStatus().toUpperCase()))
                .build();
        return mapToDTO(driverRepository.save(driver));
    }

    @Override
    public DriverResponse update(Long id, DriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setFullName(request.getFullName());
        driver.setPhone(request.getPhone());
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setStatus(Driver.Status.valueOf(request.getStatus().toUpperCase()));
        return mapToDTO(driverRepository.save(driver));
    }

    @Override
    public void delete(Long id) {
        driverRepository.deleteById(id);
    }

    private DriverResponse mapToDTO(Driver driver) {
        return DriverResponse.builder()
                .driverId(driver.getDriverId())
                .fullName(driver.getFullName())
                .phone(driver.getPhone())
                .licenseNumber(driver.getLicenseNumber())
                .status(driver.getStatus().name())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}
