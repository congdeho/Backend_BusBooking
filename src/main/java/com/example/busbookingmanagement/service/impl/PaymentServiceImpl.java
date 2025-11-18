package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.entity.Payment;
import com.example.busbookingmanagement.entity.Booking;
import com.example.busbookingmanagement.repository.PaymentRepository;
import com.example.busbookingmanagement.repository.BookingRepository;
import com.example.busbookingmanagement.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Payment createPayment(Long bookingId, String method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        Payment payment = Payment.builder()
                .booking(booking)
                .method(Payment.Method.valueOf(method))
                .amount(booking.getFinalAmount() != null ? booking.getFinalAmount() : booking.getTotalAmount())
                .status(Payment.Status.PENDING)
                .build();
        return paymentRepository.save(payment);
    }

    @Override
    public String getPaymentStatus(Long bookingId) {
        Optional<Payment> paymentOpt = paymentRepository.findByBookingBookingId(bookingId);
        return paymentOpt.map(payment -> payment.getStatus().name()).orElse("NOT_FOUND");
    }
}

