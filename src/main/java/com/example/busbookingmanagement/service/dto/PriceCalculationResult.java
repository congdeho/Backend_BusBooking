package com.example.busbookingmanagement.service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceCalculationResult {
    private int computedPrice;
    private PriceBreakdown priceBreakdown;
}
