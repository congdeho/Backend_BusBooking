package com.example.busbookingmanagement.DTO.response;

import com.example.busbookingmanagement.entity.Promotion;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponseDTO {
    private Long promotionId;
    private String code;
    private String description;
    private Promotion.DiscountType discountType;
    private Double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Promotion.Status status;
}
