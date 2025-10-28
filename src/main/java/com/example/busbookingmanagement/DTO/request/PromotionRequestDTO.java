package com.example.busbookingmanagement.DTO.request;

import com.example.busbookingmanagement.entity.Promotion;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionRequestDTO {
    private String code;
    private String description;
    private Promotion.DiscountType discountType;
    private Double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
