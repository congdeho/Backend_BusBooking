package com.example.busbookingmanagement.DTO.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRequest {
    private String startPoint;
    private String endPoint;
    private Double distanceKm;
    private Double basePrice;
    private String estimatedTime;
}
