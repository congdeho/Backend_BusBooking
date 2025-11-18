package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.entity.Payment;

public interface PaymentService {
    Payment createPayment(Long bookingId, String method);
    String getPaymentStatus(Long bookingId);
}
