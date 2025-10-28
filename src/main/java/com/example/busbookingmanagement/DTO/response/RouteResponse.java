package com.example.busbookingmanagement.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteResponse {
    private Long routeId;
    private String startPoint;
    private String endPoint;
    private Double distanceKm;
    private Double basePrice;
    private String estimatedTime;
}
