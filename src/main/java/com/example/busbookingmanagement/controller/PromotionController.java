package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.request.PromotionRequestDTO;
import com.example.busbookingmanagement.DTO.response.PromotionResponseDTO;
import com.example.busbookingmanagement.service.PromotionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion API", description = "Quản lý mã khuyến mãi và giảm giá")
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public List<PromotionResponseDTO> getAll() {
        return promotionService.getAll();
    }

    @GetMapping("/{code}")
    public ResponseEntity<PromotionResponseDTO> getByCode(@PathVariable String code) {
        return promotionService.getByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public PromotionResponseDTO create(@RequestBody PromotionRequestDTO dto) {
        return promotionService.create(dto);
    }

    @PutMapping("/{id}")
    public PromotionResponseDTO update(@PathVariable Long id, @RequestBody PromotionRequestDTO dto) {
        return promotionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        promotionService.delete(id);
    }
}
