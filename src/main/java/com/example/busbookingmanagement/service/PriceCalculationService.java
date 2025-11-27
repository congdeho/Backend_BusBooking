package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.entity.PriceTypeMultiplier;
import com.example.busbookingmanagement.repository.PriceTypeMultiplierRepository;
import com.example.busbookingmanagement.service.dto.PriceBreakdown;
import com.example.busbookingmanagement.service.dto.PriceCalculationResult;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PriceCalculationService {

    private final PriceTypeMultiplierRepository multiplierRepository;

    @Autowired
    public PriceCalculationService(PriceTypeMultiplierRepository multiplierRepository) {
        this.multiplierRepository = multiplierRepository;
    }

    // Overload used in tests could be added; keep simple for now.

    public PriceCalculationResult calculatePrice(Integer routeBasePrice, Integer schedulePrice, String busType, Integer promotionAmount) {
        int base = (routeBasePrice != null) ? routeBasePrice : ((schedulePrice != null) ? schedulePrice : 0);

        double multiplier = getMultiplierForType(busType);
        int afterType = (int) Math.round(base * multiplier);

        int promotion = (promotionAmount != null) ? promotionAmount : 0;
        int afterPromotion = afterType - promotion;
        if (afterPromotion < 0) afterPromotion = 0;

        int taxes = 0;

        PriceBreakdown breakdown = PriceBreakdown.builder()
                .base(base)
                .typeMultiplier(multiplier)
                .afterType(afterType)
                .promotion(-promotion)
                .taxes(taxes)
                .finalPrice(afterPromotion + taxes)
                .build();

        return PriceCalculationResult.builder()
                .computedPrice(breakdown.getFinalPrice())
                .priceBreakdown(breakdown)
                .build();
    }

    private double getMultiplierForType(String busType) {
        String normalized = normalize(busType == null ? "" : busType);
        List<PriceTypeMultiplier> candidates = null;
        try {
            candidates = multiplierRepository.findByActiveTrueOrderByPriorityDesc();
        } catch (Exception ignored) {
            // repository might be empty; fallback
        }

        if (candidates == null || candidates.isEmpty()) {
            // fallback rule for "giường"
            if (normalized.matches(".*\\b(giuong|giuong nam|giuong nam|giuong|giuong nam)\\b.*")) return 1.4;
            return 1.0;
        }

        for (PriceTypeMultiplier p : candidates) {
            try {
                Pattern pat = Pattern.compile(p.getMatchRegex(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                if (pat.matcher(normalized).find()) {
                    return p.getMultiplier();
                }
            } catch (Exception e) {
                // invalid regex -> skip
            }
        }
        return 1.0;
    }

    private String normalize(String input) {
        String n = Normalizer.normalize(input == null ? "" : input, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", "");
        return n.toLowerCase();
    }
}

