package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.request.PromotionRequestDTO;
import com.example.busbookingmanagement.DTO.response.PromotionResponseDTO;
import com.example.busbookingmanagement.entity.Promotion;
import com.example.busbookingmanagement.repository.PromotionRepository;
import com.example.busbookingmanagement.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    private PromotionResponseDTO mapToDTO(Promotion p) {
        return PromotionResponseDTO.builder()
                .promotionId(p.getPromotionId())
                .code(p.getCode())
                .description(p.getDescription())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .status(p.getStatus())
                .build();
    }

    @Override
    public List<PromotionResponseDTO> getAll() {
        return promotionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PromotionResponseDTO> getByCode(String code) {
        return promotionRepository.findByCode(code).map(this::mapToDTO);
    }

    @Override
    public PromotionResponseDTO create(PromotionRequestDTO dto) {
        Promotion promotion = Promotion.builder()
                .code(dto.getCode())
                .description(dto.getDescription())
                .discountType(dto.getDiscountType())
                .discountValue(dto.getDiscountValue())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(Promotion.Status.ACTIVE)
                .build();

        Promotion saved = promotionRepository.save(promotion);
        return mapToDTO(saved);
    }

    @Override
    public PromotionResponseDTO update(Long id, PromotionRequestDTO dto) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        promotion.setCode(dto.getCode());
        promotion.setDescription(dto.getDescription());
        promotion.setDiscountType(dto.getDiscountType());
        promotion.setDiscountValue(dto.getDiscountValue());
        promotion.setStartDate(dto.getStartDate());
        promotion.setEndDate(dto.getEndDate());

        Promotion updated = promotionRepository.save(promotion);
        return mapToDTO(updated);
    }

    @Override
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }
}
