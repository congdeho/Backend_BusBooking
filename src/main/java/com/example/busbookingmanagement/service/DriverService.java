package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.request.DriverRequest;
import com.example.busbookingmanagement.DTO.response.DriverResponse;

import java.util.List;

public interface DriverService {
    List<DriverResponse> getAll();
    DriverResponse getById(Long id);
    DriverResponse create(DriverRequest request);
    DriverResponse update(Long id, DriverRequest request);
    void delete(Long id);
}
