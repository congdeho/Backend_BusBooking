package com.example.busbookingmanagement.controller;

import com.example.busbookingmanagement.service.PaymentService;
import com.example.busbookingmanagement.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // POST /api/payments/create – Tạo giao dịch
    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(@RequestBody Map<String, Object> request) {
        Long bookingId = Long.valueOf(request.get("bookingId").toString());
        String method = request.get("method").toString();
        return ResponseEntity.ok(paymentService.createPayment(bookingId, method));
    }

    // GET /api/payments/status/{bookingId} – Kiểm tra trạng thái
    @GetMapping("/status/{bookingId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(bookingId));
    }
}

