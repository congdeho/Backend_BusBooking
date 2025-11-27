package com.example.busbookingmanagement.service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceBreakdown {
    private int base;                // base price (VND)
    private double typeMultiplier;   // e.g. 1.4
    private int afterType;           // rounded(base * multiplier)
    private int promotion;           // negative or zero (representing discount)
    private int taxes;               // taxes (VND)
    private int finalPrice;          // final computed price (VND)
}
