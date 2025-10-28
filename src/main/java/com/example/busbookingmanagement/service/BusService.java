package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.response.BusResponse;
import com.example.busbookingmanagement.entity.Bus;

import java.util.List;
import java.util.Optional;

public interface BusService {
    List<BusResponse> getAll();
    Optional<BusResponse> getById(Long id);
    Optional<BusResponse> getByLicensePlate(String licensePlate);
    Bus create(Bus bus);
    Bus update(Long id, Bus bus);
    void delete(Long id);
}
