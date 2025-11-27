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

    // POST /api/payments/checkout – Trả về VNPAY checkout URL (redirect)
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(@RequestBody Map<String, Object> request) {
        Long bookingId = Long.valueOf(request.get("bookingId").toString());
        String url = paymentService.createPaymentUrl(bookingId);
        return ResponseEntity.ok(Map.of("paymentUrl", url));
    }

    // POST /api/payment/vnpay/confirm - VNPay will redirect frontend and frontend posts params here
    @PostMapping(path = "/vnpay/confirm", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Map<String, Object>> vnpayConfirm(@RequestParam Map<String, String> allParams) {
        // delegate to service to verify and process payment; service returns map with status and message
        Map<String, Object> result = paymentService.processVnPayReturn(allParams);
        return ResponseEntity.ok(result);
    }
}
