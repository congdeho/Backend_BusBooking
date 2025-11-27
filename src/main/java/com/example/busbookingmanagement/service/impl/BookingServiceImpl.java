package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.DTO.response.BookingResponse;
import com.example.busbookingmanagement.entity.*;
import com.example.busbookingmanagement.repository.BookingRepository;
import com.example.busbookingmanagement.repository.SeatRepository;
import com.example.busbookingmanagement.repository.BookingSeatRepository;
import com.example.busbookingmanagement.repository.ScheduleRepository;
import com.example.busbookingmanagement.repository.TicketRepository;
import com.example.busbookingmanagement.repository.PaymentRepository;
import com.example.busbookingmanagement.repository.InvoiceRepository;
import com.example.busbookingmanagement.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ScheduleRepository scheduleRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public List<BookingResponse> getAll() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponse getById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return mapToDTO(booking);
    }

    @Override
    public BookingResponse create(Booking booking) {
        // Ensure schedule relation is fully loaded (so booking.getSchedule().getRoute() is not null)
        if (booking.getSchedule() != null && booking.getSchedule().getScheduleId() != null) {
            Schedule sched = scheduleRepository.findById(booking.getSchedule().getScheduleId())
                    .orElseThrow(() -> new EntityNotFoundException("Schedule not found"));
            booking.setSchedule(sched);
        }

        Booking saved = bookingRepository.save(booking);
        return mapToDTO(saved);
    }

    @Override
    public BookingResponse update(Long id, Booking booking) {
        Booking updated = bookingRepository.findById(id)
                .map(b -> {
                    b.setStatus(booking.getStatus());
                    b.setFinalAmount(booking.getFinalAmount());
                    b.setTotalAmount(booking.getTotalAmount());
                    b.setDiscountAmount(booking.getDiscountAmount());
                    return bookingRepository.save(b);
                })
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return mapToDTO(updated);
    }

    @Override
    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public List<BookingResponse> getByUserId(Long userId) {
        return bookingRepository.findAll()
                .stream()
                .filter(b -> b.getUser() != null && userId.equals(b.getUser().getUserId()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void selectSeats(Long bookingId, List<Long> seatIds) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        List<Seat> seats = seatRepository.findAllById(seatIds);
        for (Seat seat : seats) {
            if (seat.getStatus() != Seat.Status.AVAILABLE) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available");
            }
            // Do NOT change Seat.status here. Only persist booking-seat association (PENDING reservation)
            Booking bookingRef = bookingRepository.findById(bookingId).orElseThrow(() -> new EntityNotFoundException("Booking not found"));
            BookingSeat bs = BookingSeat.builder()
                    .booking(bookingRef)
                    .seat(seat)
                    .build();
            bookingSeatRepository.save(bs);
        }
    }

    @Override
    @Transactional
    public void cancelSeat(Long bookingId, Long seatId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found"));
        seat.setStatus(Seat.Status.AVAILABLE);
        seatRepository.save(seat);
        // TODO: Xóa ticket booking-seat nếu cần
    }

    @Override
    @Transactional
    public BookingResponse createPendingBookingWithSeats(Booking booking, List<Long> seatIds) {
        // ensure seats exist and are available for the schedule
        Long scheduleId = booking.getSchedule() != null ? booking.getSchedule().getScheduleId() : null;
        if (scheduleId == null) throw new IllegalArgumentException("Schedule must be provided");

        List<Seat> seats = seatRepository.findSeatsForUpdateByIdsAndScheduleId(seatIds, scheduleId);
        if (seats.size() != seatIds.size()) {
            throw new IllegalStateException("One or more seats do not belong to the schedule or do not exist");
        }
        for (Seat s : seats) {
            if (s.getStatus() != Seat.Status.AVAILABLE) {
                throw new IllegalStateException("Seat " + s.getSeatNumber() + " is not available");
            }
        }

        // save booking (PENDING)
        booking.setStatus(Booking.Status.PENDING);
        Booking saved = bookingRepository.save(booking);

        // persist booking-seat associations (no change to seat.status yet)
        for (Seat s : seats) {
            BookingSeat bs = BookingSeat.builder()
                    .booking(saved)
                    .seat(s)
                    .build();
            bookingSeatRepository.save(bs);
        }

        return mapToDTO(saved);
    }

    @Override
    public com.example.busbookingmanagement.DTO.response.BookingDetailsResponse getBookingDetails(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        // booking seats
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingBookingId(bookingId);

        // tickets
        List<Ticket> tickets = ticketRepository.findByBookingBookingId(bookingId);

        // payment (latest or any)
        java.util.Optional<Payment> paymentOpt = java.util.Optional.empty();
        try {
            paymentOpt = java.util.Optional.ofNullable(paymentRepository.findByBookingBookingId(bookingId).orElse(null));
        } catch (Exception ignored) {}

        // invoice (try to find via payment)
        java.util.Optional<Invoice> invoiceOpt = java.util.Optional.empty();
        if (paymentOpt.isPresent()) {
            try {
                // extract paymentId into a local final variable so it can be used inside lambda
                final Long paymentId = paymentOpt.get().getPaymentId();
                // invoiceRepository doesn't have a finder by paymentId; search by paymentId via repository
                invoiceOpt = invoiceRepository.findAll().stream()
                        .filter(inv -> inv.getPayment() != null && paymentId.equals(inv.getPayment().getPaymentId()))
                        .findFirst();
            } catch (Exception ignored) {}
        }

        // map booking -> DTO
        com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.UserInfo userInfo = com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.UserInfo.builder()
                .userId(booking.getUser() != null ? booking.getUser().getUserId() : null)
                .fullName(booking.getUser() != null ? booking.getUser().getFullName() : null)
                .email(booking.getUser() != null ? booking.getUser().getEmail() : null)
                .phone(booking.getUser() != null ? booking.getUser().getPhone() : null)
                .build();

        com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.ScheduleInfo scheduleInfo = null;
        if (booking.getSchedule() != null) {
            String routeStr = null;
            if (booking.getSchedule().getRoute() != null) {
                routeStr = booking.getSchedule().getRoute().getStartPoint() + " - " + booking.getSchedule().getRoute().getEndPoint();
            }
            scheduleInfo = com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.ScheduleInfo.builder()
                    .scheduleId(booking.getSchedule().getScheduleId())
                    .route(routeStr)
                    .departureTime(booking.getSchedule().getDepartureTime())
                    .arrivalTime(booking.getSchedule().getArrivalTime())
                    .build();
        }

        List<com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.SeatInfo> seatInfos = bookingSeats.stream().map(bs ->
                com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.SeatInfo.builder()
                        .bookingSeatId(bs.getId())
                        .seatId(bs.getSeat() != null ? bs.getSeat().getSeatId() : null)
                        .seatNumber(bs.getSeat() != null ? bs.getSeat().getSeatNumber() : null)
                        .seatStatus(bs.getSeat() != null ? bs.getSeat().getStatus().name() : null)
                        .build()
        ).toList();

        List<com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.TicketInfo> ticketInfos = tickets.stream().map(t ->
                com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.TicketInfo.builder()
                        .ticketId(t.getTicketId())
                        .seatId(t.getSeat() != null ? t.getSeat().getSeatId() : null)
                        .seatNumber(t.getSeat() != null ? t.getSeat().getSeatNumber() : null)
                        .price(t.getPrice() != null ? new java.math.BigDecimal(t.getPrice().toString()) : null)
                        .qrCode(t.getQrCode())
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .build()
        ).toList();

        com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.PaymentInfo paymentInfo = null;
        if (paymentOpt.isPresent()) {
            Payment p = paymentOpt.get();
            paymentInfo = com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.PaymentInfo.builder()
                    .paymentId(p.getPaymentId())
                    .method(p.getMethod() != null ? p.getMethod().name() : null)
                    .amount(p.getAmount())
                    .transactionId(p.getTransactionId())
                    .status(p.getStatus() != null ? p.getStatus().name() : null)
                    .paymentTime(p.getPaymentTime())
                    .build();
        }

        com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.InvoiceInfo invoiceInfo = null;
        if (invoiceOpt.isPresent()) {
            Invoice inv = invoiceOpt.get();
            invoiceInfo = com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.InvoiceInfo.builder()
                    .invoiceId(inv.getInvoiceId())
                    .invoiceNumber(inv.getInvoiceNumber())
                    .total(inv.getTotal())
                    .tax(inv.getTax())
                    .issuedDate(inv.getIssuedDate())
                    .build();
        }

        com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.Meta meta = com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.Meta.builder()
                .numberOfSeats(seatInfos.size())
                .canPayAgain(booking.getStatus() == Booking.Status.PENDING)
                .build();

        return com.example.busbookingmanagement.DTO.response.BookingDetailsResponse.builder()
                .bookingId(booking.getBookingId())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .bookingDate(booking.getBookingDate())
                .totalAmount(booking.getTotalAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .user(userInfo)
                .schedule(scheduleInfo)
                .seats(seatInfos)
                .tickets(ticketInfos)
                .payment(paymentInfo)
                .invoice(invoiceInfo)
                .meta(meta)
                .build();
    }

    // Helper method: convert entity → DTO
    private BookingResponse mapToDTO(Booking booking) {
        String routeStr = null;
        if (booking.getSchedule() != null) {
            Schedule schedule = booking.getSchedule();
            Route route = schedule.getRoute();
            if (route != null) {
                String start = route.getStartPoint();
                String end = route.getEndPoint();
                routeStr = (start != null ? start : "") + (start != null && end != null ? " - " : "") + (end != null ? end : "");
            }
        }

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userName(booking.getUser() != null ? booking.getUser().getFullName() : null)
                .route(routeStr)
                .bookingDate(booking.getBookingDate())
                .finalAmount(booking.getFinalAmount())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .build();
    }
}
