package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.DTO.response.AdminOverviewResponse;
import com.example.busbookingmanagement.entity.*;
import com.example.busbookingmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final BusRepository busRepository;
    private final ScheduleRepository scheduleRepository;

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> overview() {
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();
        long totalPayments = paymentRepository.count();
        long totalTickets = ticketRepository.count();
        long totalBuses = 0L;
        try { totalBuses = busRepository.count(); } catch (Exception ignored) {}

        // safe comparator for bookingDate (nulls last) and descending order
        Comparator<Booking> bookingDateDesc = (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            java.time.LocalDateTime ad = a.getBookingDate();
            java.time.LocalDateTime bd = b.getBookingDate();
            if (ad == null && bd == null) return 0;
            if (ad == null) return 1;
            if (bd == null) return -1;
            return bd.compareTo(ad); // descending
        };

        List<AdminOverviewResponse.BookingItem> recentBookings = bookingRepository.findAll().stream()
                .filter(java.util.Objects::nonNull)
                .sorted(bookingDateDesc)
                .limit(10)
                .map(b -> AdminOverviewResponse.BookingItem.builder()
                        .bookingId(b.getBookingId())
                        .status(b.getStatus() != null ? b.getStatus().name() : null)
                        .finalAmount(b.getFinalAmount())
                        .bookingDate(b.getBookingDate())
                        .userId(b.getUser() != null ? b.getUser().getUserId() : null)
                        .userName(b.getUser() != null ? b.getUser().getFullName() : null)
                        .build())
                .collect(Collectors.toList());

        List<AdminOverviewResponse.UserItem> recentUsers = userRepository.findAll().stream()
                // `User` entity does not have a createdAt timestamp; use userId descending as a proxy for recent users
                .filter(java.util.Objects::nonNull)
                .sorted((a, b) -> b.getUserId().compareTo(a.getUserId()))
                .limit(10)
                .map(u -> AdminOverviewResponse.UserItem.builder()
                        .userId(u.getUserId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .build())
                .collect(Collectors.toList());

        // safe comparator for paymentTime (nulls last) and descending order
        Comparator<Payment> paymentTimeDesc = (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            java.time.LocalDateTime at = a.getPaymentTime();
            java.time.LocalDateTime bt = b.getPaymentTime();
            if (at == null && bt == null) return 0;
            if (at == null) return 1; // put nulls last
            if (bt == null) return -1;
            return bt.compareTo(at); // descending
        };

        List<AdminOverviewResponse.PaymentItem> recentPayments = paymentRepository.findAll().stream()
                .filter(java.util.Objects::nonNull)
                .sorted(paymentTimeDesc)
                .limit(10)
                .map(p -> AdminOverviewResponse.PaymentItem.builder()
                        .paymentId(p.getPaymentId())
                        .method(p.getMethod() != null ? p.getMethod().name() : null)
                        .amount(p.getAmount())
                        .status(p.getStatus() != null ? p.getStatus().name() : null)
                        .paymentTime(p.getPaymentTime())
                        .build())
                .collect(Collectors.toList());

        List<AdminOverviewResponse.TicketItem> recentTickets = ticketRepository.findAll().stream()
                .filter(java.util.Objects::nonNull)
                .sorted((a,b) -> b.getTicketId().compareTo(a.getTicketId()))
                .limit(10)
                .map(t -> AdminOverviewResponse.TicketItem.builder()
                        .ticketId(t.getTicketId())
                        .bookingId(t.getBooking() != null ? t.getBooking().getBookingId() : null)
                        .seatId(t.getSeat() != null ? t.getSeat().getSeatId() : null)
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());

        // total revenue = sum of successful payments
        double totalRevenue = paymentRepository.findAll().stream()
                .filter(java.util.Objects::nonNull)
                .filter(p -> p.getStatus() == Payment.Status.SUCCESS)
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        // revenue time series for last 30 days (by paymentTime)
        java.time.LocalDate today = java.time.LocalDate.now();
        int days = 30;
        List<AdminOverviewResponse.TimeSeriesPoint> revenueSeries = java.util.stream.IntStream.rangeClosed(0, days-1)
                .mapToObj(i -> today.minusDays(days-1 - i))
                .map(date -> {
                    double sum = paymentRepository.findAll().stream()
                            .filter(p -> p.getStatus() == Payment.Status.SUCCESS && p.getPaymentTime() != null)
                            .filter(p -> p.getPaymentTime().toLocalDate().isEqual(date))
                            .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                            .sum();
                    return AdminOverviewResponse.TimeSeriesPoint.builder().date(date).value(sum).build();
                }).collect(Collectors.toList());

        // bus type distribution based on tickets -> seat -> bus.busType
        List<Ticket> allTickets = ticketRepository.findAll();
        java.util.Map<String, Long> busTypeCounts = allTickets.stream()
                .map(t -> t.getSeat() != null && t.getSeat().getBus() != null ? t.getSeat().getBus().getBusType() : "UNKNOWN")
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        long totalTicketCount = allTickets.isEmpty() ? 1 : allTickets.size(); // avoid divide by zero
        List<AdminOverviewResponse.BusTypeStat> busTypeStats = busTypeCounts.entrySet().stream()
                .map(e -> AdminOverviewResponse.BusTypeStat.builder()
                        .busType(e.getKey())
                        .count(e.getValue())
                        .percent(100.0 * e.getValue() / totalTicketCount)
                        .build())
                .collect(Collectors.toList());

        // Top schedules by revenue: sum payments grouped by schedule (via booking -> schedule)
        java.util.Map<Long, Double> revenueBySchedule = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.Status.SUCCESS && p.getBooking() != null && p.getBooking().getSchedule() != null)
                .collect(Collectors.groupingBy(p -> p.getBooking().getSchedule().getScheduleId(), Collectors.summingDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)));

        List<AdminOverviewResponse.TopSchedule> topSchedules = revenueBySchedule.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(e -> {
                    Long schedId = e.getKey();
                    Schedule sched = scheduleRepository.findById(schedId).orElse(null);
                    String routeStr = null;
                    if (sched != null && sched.getRoute() != null) {
                        routeStr = sched.getRoute().getStartPoint() + " - " + sched.getRoute().getEndPoint();
                    }
                    return AdminOverviewResponse.TopSchedule.builder()
                            .scheduleId(schedId)
                            .route(routeStr)
                            .revenue(e.getValue())
                            .build();
                }).collect(Collectors.toList());

        AdminOverviewResponse.Stats stats = AdminOverviewResponse.Stats.builder()
                .totalUsers(totalUsers)
                .totalBookings(totalBookings)
                .totalPayments(totalPayments)
                .totalTickets(totalTickets)
                .totalBuses(totalBuses)
                .build();

        AdminOverviewResponse resp = AdminOverviewResponse.builder()
                .stats(stats)
                .recentBookings(recentBookings)
                .recentUsers(recentUsers)
                .recentPayments(recentPayments)
                .recentTickets(recentTickets)
                .totalRevenue(totalRevenue)
                .revenueTimeSeries(revenueSeries)
                .busTypeDistribution(busTypeStats)
                .topSchedules(topSchedules)
                .build();

        return ResponseEntity.ok(resp);
    }
}
