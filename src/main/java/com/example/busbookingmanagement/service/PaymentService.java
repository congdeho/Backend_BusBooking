package com.example.busbookingmanagement.service;

import com.example.busbookingmanagement.entity.Payment;

import java.util.Map;

public interface PaymentService {
    Payment createPayment(Long bookingId, String method);
    String getPaymentStatus(Long bookingId);
    // create a signed VNPAY payment URL for booking (backend returnUrl from config)
    String createPaymentUrl(Long bookingId);

    // Process VNPay return params (verify secure hash and finalize booking & payments)
    Map<String, Object> processVnPayReturn(Map<String, String> vnpParams);
}
