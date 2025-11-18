package com.example.busbookingmanagement.DTO.response;

import com.example.busbookingmanagement.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Long seatId;
    private String seatNumber;
    private Seat.Status status;
}
