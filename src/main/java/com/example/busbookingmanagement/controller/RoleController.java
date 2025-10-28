package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.request.RoleRequestDTO;
import com.example.busbookingmanagement.DTO.response.RoleResponseDTO;
import com.example.busbookingmanagement.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role API", description = "Quản lý quyền người dùng (Admin, Staff, Customer)")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponseDTO> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getById(@PathVariable Long id) {
        return roleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<RoleResponseDTO> getByName(@PathVariable String roleName) {
        return roleService.getByName(roleName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RoleResponseDTO create(@RequestBody RoleRequestDTO dto) {
        return roleService.create(dto);
    }

    @PutMapping("/{id}")
    public RoleResponseDTO update(@PathVariable Long id, @RequestBody RoleRequestDTO dto) {
        return roleService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
