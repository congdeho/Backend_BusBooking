package com.example.busbookingmanagement.DTO.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOverviewResponse {
    private Stats stats;
    private List<BookingItem> recentBookings;
    private List<UserItem> recentUsers;
    private List<PaymentItem> recentPayments;
    private List<TicketItem> recentTickets;

    // New fields for dashboard charts
    private Double totalRevenue;
    private List<TimeSeriesPoint> revenueTimeSeries; // e.g., last 30 days
    private List<BusTypeStat> busTypeDistribution; // pie chart percentages
    private List<TopSchedule> topSchedules; // top schedules by revenue

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Stats {
        private Long totalUsers;
        private Long totalBookings;
        private Long totalPayments;
        private Long totalTickets;
        private Long totalBuses;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingItem {
        private Long bookingId;
        private String status;
        private Double finalAmount;
        private LocalDateTime bookingDate;
        private Long userId;
        private String userName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserItem {
        private Long userId;
        private String fullName;
        private String email;
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentItem {
        private Long paymentId;
        private String method;
        private Double amount;
        private String status;
        private LocalDateTime paymentTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TicketItem {
        private Long ticketId;
        private Long bookingId;
        private Long seatId;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesPoint {
        private LocalDate date;
        private Double value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusTypeStat {
        private String busType;
        private Long count;
        private Double percent; // 0..100
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopSchedule {
        private Long scheduleId;
        private String route;
        private Double revenue;
    }
}
