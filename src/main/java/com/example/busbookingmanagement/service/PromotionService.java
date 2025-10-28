package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.DTO.request.PromotionRequestDTO;
import com.example.busbookingmanagement.DTO.response.PromotionResponseDTO;

import java.util.List;
import java.util.Optional;

public interface PromotionService {
    List<PromotionResponseDTO> getAll();
    Optional<PromotionResponseDTO> getByCode(String code);
    PromotionResponseDTO create(PromotionRequestDTO dto);
    PromotionResponseDTO update(Long id, PromotionRequestDTO dto);
    void delete(Long id);
}
