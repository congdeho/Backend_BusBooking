package com.example.busbookingmanagement.DTO.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDetailsResponse {
    private Long bookingId;
    private String status;
    private LocalDateTime bookingDate;
    private Double totalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private UserInfo user;
    private ScheduleInfo schedule;
    private List<SeatInfo> seats;
    private List<TicketInfo> tickets;
    private PaymentInfo payment;
    private InvoiceInfo invoice;
    private Meta meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String fullName;
        private String email;
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleInfo {
        private Long scheduleId;
        private String route;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatInfo {
        private Long bookingSeatId;
        private Long seatId;
        private String seatNumber;
        private String seatStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketInfo {
        private Long ticketId;
        private Long seatId;
        private String seatNumber;
        private BigDecimal price;
        private String qrCode;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private Long paymentId;
        private String method;
        private Double amount;
        private String transactionId;
        private String status;
        private LocalDateTime paymentTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceInfo {
        private Long invoiceId;
        private String invoiceNumber;
        private Double total;
        private Double tax;
        private LocalDateTime issuedDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Meta {
        private Integer numberOfSeats;
        private Boolean canPayAgain;
    }
}

