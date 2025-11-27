package com.example.busbookingmanagement.service.impl;

import com.example.busbookingmanagement.entity.*;
import com.example.busbookingmanagement.repository.*;
import com.example.busbookingmanagement.service.PaymentService;
import com.example.busbookingmanagement.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final ScheduleRepository scheduleRepository;
    private final InvoiceRepository invoiceRepository;
    private final NotificationRepository notificationRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final MailService mailService;

    @Value("${vnpay.tmnCode:5JI0KL4O}")
    private String vnpTmnCode;

    @Value("${vnpay.returnUrl:http://localhost:3000/payment/result}")
    private String defaultReturnUrl;

    @Value("${vnpay.baseUrl:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpBaseUrl;

    @Value("${vnpay.hashSecret:PMDBP4XUP0SA0Q9CLIM6EQF1TULTF60N}")
    private String vnpHashSecret;

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

    @Override
    public String createPaymentUrl(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // amount in smallest currency unit (VND * 100)
        long vnpAmount = Math.round((booking.getFinalAmount() != null ? booking.getFinalAmount() : booking.getTotalAmount()) * 100.0);

        // build params map (like demo)
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", String.valueOf(bookingId));
        // No space before colon as in demo
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + bookingId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", defaultReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // Use explicit VN timezone to avoid confusion with Etc/GMT +/- semantics
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh"); // recommended for Vietnam
        LocalDateTime now = LocalDateTime.now(zone);

        // create and expire times
        String vnpCreateDate = now.format(formatter);
        String vnpExpireDate = now.plusMinutes(15).format(formatter);

        vnp_Params.put("vnp_CreateDate", vnpCreateDate);
        vnp_Params.put("vnp_ExpireDate", vnpExpireDate);

        // (Optional) billing/invoice fields can be added similarly if available

        // sort keys
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                     // build hashData (use encoded values)
                     hashData.append(fieldName).append("=").append(encodedValue);
                     // build query (encode key and value)
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append("=")
                            .append(encodedValue);
                     if (itr.hasNext()) {
                         query.append('&');
                         hashData.append('&');
                     }
                 } catch (Exception ex) {
                     throw new RuntimeException("Encoding error", ex);
                 }
             }
         }

         String queryUrl = query.toString();
         String vnp_SecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
         queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
         String paymentUrl = vnpBaseUrl + "?" + queryUrl;

         System.out.println("============= VNPAY BUILD DEBUG =============");
         System.out.println("Sorted keys = " + fieldNames);
         System.out.println("[DEBUG] hashData BEFORE HASH = " + hashData);
         System.out.println("[DEBUG] secureHash GENERATED = " + vnp_SecureHash);
         System.out.println("=============================================");
         System.out.println("[VnPay] Built paymentUrl: " + paymentUrl);

         return paymentUrl;
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to build HMAC SHA512", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> processVnPayReturn(Map<String, String> vnpParams) {
        Map<String, Object> resp = new HashMap<>();

//        if (!VnPayUtil.verifySecureHash(vnpParams, vnpHashSecret)) {
//            resp.put("success", false);
//            resp.put("message", "Invalid secure hash");
//            return resp;
//        }

        String responseCode = vnpParams.getOrDefault("vnp_ResponseCode", "");
        String txnRef = vnpParams.get("vnp_TxnRef");
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        String amountStr = vnpParams.get("vnp_Amount");
        Long bookingId = null;
        try { bookingId = Long.valueOf(txnRef); } catch (Exception ignored) {}

        if (bookingId == null) {
            resp.put("success", false);
            resp.put("message", "Missing booking reference");
            return resp;
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        double amount = 0.0;
        try {
            if (amountStr != null && !amountStr.isBlank()) {
                amount = Long.parseLong(amountStr) / 100.0;
            }
        } catch (Exception ignored) {}

        if (!"00".equals(responseCode)) {
            paymentRepository.save(Payment.builder()
                    .booking(booking)
                    .method(Payment.Method.VNPAY)
                    .transactionId(transactionNo)
                    .amount(amount)
                    .status(Payment.Status.FAILED)
                    .build());

            booking.setStatus(Booking.Status.CANCELLED);
            bookingRepository.save(booking);

            resp.put("success", false);
            resp.put("message", "Payment failed with code " + responseCode);
            return resp;
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .booking(booking)
                .method(Payment.Method.VNPAY)
                .transactionId(transactionNo)
                .amount(amount)
                // set explicit payment time so the builder doesn't leave it null
                .paymentTime(LocalDateTime.now())
                .status(Payment.Status.SUCCESS)
                .build());

        booking.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingBookingId(bookingId);
        List<Long> seatIds = bookingSeats.stream().map(bs -> bs.getSeat().getSeatId()).toList();

        if (!seatIds.isEmpty()) {
            // Lock seat rows for this schedule
            List<Seat> lockedSeats = seatRepository.findSeatsForUpdateByIdsAndScheduleId(
                    seatIds, booking.getSchedule().getScheduleId()
            );

            // build map seatId -> locked Seat for quick lookup
            Map<Long, Seat> lockedById = new HashMap<>();
            for (Seat s : lockedSeats) lockedById.put(s.getSeatId(), s);

            // Verify availability for all requested seats
            for (BookingSeat bs : bookingSeats) {
                Long sId = bs.getSeat().getSeatId();
                Seat locked = lockedById.get(sId);
                if (locked == null) {
                    throw new RuntimeException("Seat not found or not part of schedule: " + sId);
                }
//                if (locked.getStatus() != Seat.Status.AVAILABLE) {
//                    throw new RuntimeException("Seat not available: " + locked.getSeatNumber());
//                }
            }

            // Create tickets in the same order as bookingSeats — ticket references the locked Seat
            List<Ticket> tickets = new ArrayList<>();
            List<Seat> toSaveSeats = new ArrayList<>();
            for (BookingSeat bs : bookingSeats) {
                Long sId = bs.getSeat().getSeatId();
                Seat locked = lockedById.get(sId);
                // mark seat booked and create ticket
                locked.setStatus(Seat.Status.BOOKED);
                toSaveSeats.add(locked);

                tickets.add(Ticket.builder()
                        .booking(booking)
                        .seat(locked)
                        .qrCode(UUID.randomUUID().toString())
                        .status(Ticket.Status.BOOKED)
                        .build());
            }

            ticketRepository.saveAll(tickets);
            seatRepository.saveAll(toSaveSeats);

            Schedule schedule = booking.getSchedule();
            schedule.setAvailableSeats(schedule.getAvailableSeats() - toSaveSeats.size());
            scheduleRepository.save(schedule);
        }

        Invoice invoice = invoiceRepository.save(Invoice.builder()
                .payment(payment)
                .tax(0.0)
                .total(payment.getAmount())
                .invoiceNumber("INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                        "-" + payment.getPaymentId())
                .build());

        notificationRepository.save(Notification.builder()
                .user(booking.getUser())
                .title("Thanh toán thành công")
                .message("Đơn đặt vé " + booking.getBookingId() + " đã thanh toán thành công.")
                .type(Notification.Type.EMAIL)
                .build());

        // send email to the user — best-effort, don't fail payment flow if mail fails
        try {
            String to = booking.getUser() != null ? booking.getUser().getEmail() : null;
            if (to != null && !to.isBlank()) {
                String subject = "[BusBooking] Thanh toán thành công - Đơn " + booking.getBookingId();
                StringBuilder body = new StringBuilder();
                body.append("Xin chúc mừng,\n\n");
                body.append("Đơn đặt vé ").append(booking.getBookingId()).append(" đã được thanh toán thành công.\n");
                body.append("Mã thanh toán: ").append(payment.getPaymentId()).append("\n");
                body.append("Số tiền: ").append(String.format("%.2f", payment.getAmount())).append(" VND\n");
                body.append("Số hóa đơn: ").append(invoice.getInvoiceNumber()).append("\n\n");
                body.append("Cảm ơn bạn đã sử dụng dịch vụ.");

                mailService.sendPaymentConfirmation(to, subject, body.toString());
            }
        } catch (Exception ex) {
            // do not interrupt payment processing
            System.err.println("Error sending payment confirmation email: " + ex.getMessage());
        }

        resp.put("success", true);
        resp.put("paymentId", payment.getPaymentId());
        resp.put("bookingId", booking.getBookingId());
        resp.put("invoiceId", invoice.getInvoiceId());
        resp.put("message", "Payment confirmed");

        return resp;
    }
}
