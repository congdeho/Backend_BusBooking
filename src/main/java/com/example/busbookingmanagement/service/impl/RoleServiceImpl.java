package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.request.RoleRequestDTO;
import com.example.busbookingmanagement.DTO.response.RoleResponseDTO;
import com.example.busbookingmanagement.entity.Role;
import com.example.busbookingmanagement.repository.RoleRepository;
import com.example.busbookingmanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private RoleResponseDTO mapToDTO(Role role) {
        return RoleResponseDTO.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build();
    }

    @Override
    public List<RoleResponseDTO> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RoleResponseDTO> getById(Long id) {
        return roleRepository.findById(id).map(this::mapToDTO);
    }

    @Override
    public Optional<RoleResponseDTO> getByName(String roleName) {
        return roleRepository.findByRoleName(roleName).map(this::mapToDTO);
    }

    @Override
    public RoleResponseDTO create(RoleRequestDTO dto) {
        Role role = Role.builder()
                .roleName(dto.getRoleName())
                .description(dto.getDescription())
                .build();
        return mapToDTO(roleRepository.save(role));
    }

    @Override
    public RoleResponseDTO update(Long id, RoleRequestDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());

        return mapToDTO(roleRepository.save(role));
    }

    @Override
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}
