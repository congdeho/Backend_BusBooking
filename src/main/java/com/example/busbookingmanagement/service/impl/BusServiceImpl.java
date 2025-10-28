package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.response.BusResponse;
import com.example.busbookingmanagement.entity.Bus;
import com.example.busbookingmanagement.repository.BusRepository;
import com.example.busbookingmanagement.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;

    private BusResponse mapToDTO(Bus bus) {
        return BusResponse.builder()
                .busId(bus.getBusId())
                .licensePlate(bus.getLicensePlate())
                .busType(bus.getBusType())
                .totalSeats(bus.getTotalSeats())
                .driverName(bus.getDriver() != null ? bus.getDriver().getFullName() : null)
                .driverPhone(bus.getDriver() != null ? bus.getDriver().getPhone() : null)
                .status(bus.getStatus().name())
                .build();
    }


    @Override
    public List<BusResponse> getAll() {
        return busRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BusResponse> getById(Long id) {
        return busRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public Optional<BusResponse> getByLicensePlate(String licensePlate) {
        return busRepository.findByLicensePlate(licensePlate).map(this::mapToDTO);
    }

    @Override
    public Bus create(Bus bus) {
        return busRepository.save(bus);
    }

    @Override
    public Bus update(Long id, Bus bus) {
        return busRepository.findById(id)
                .map(existingBus -> {
                    existingBus.setLicensePlate(bus.getLicensePlate());
                    existingBus.setBusType(bus.getBusType());
                    existingBus.setTotalSeats(bus.getTotalSeats());
                    existingBus.setStatus(bus.getStatus());

                    // Cập nhật tài xế (nếu có)
                    if (bus.getDriver() != null) {
                        existingBus.setDriver(bus.getDriver());
                    }

                    return busRepository.save(existingBus);
                })
                .orElseThrow(() -> new RuntimeException("Bus not found"));
    }


    @Override
    public void delete(Long id) {
        busRepository.deleteById(id);
    }
}
