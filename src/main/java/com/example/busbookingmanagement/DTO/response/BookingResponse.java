package com.example.busbookingmanagement.DTO.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String userName;
    private String route;
    private LocalDateTime bookingDate;
    private Double finalAmount;
    private String status;
}
