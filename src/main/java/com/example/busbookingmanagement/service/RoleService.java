package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.request.RoleRequestDTO;
import com.example.busbookingmanagement.DTO.response.RoleResponseDTO;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<RoleResponseDTO> getAll();
    Optional<RoleResponseDTO> getById(Long id);
    Optional<RoleResponseDTO> getByName(String roleName);
    RoleResponseDTO create(RoleRequestDTO dto);
    RoleResponseDTO update(Long id, RoleRequestDTO dto);
    void delete(Long id);
}
