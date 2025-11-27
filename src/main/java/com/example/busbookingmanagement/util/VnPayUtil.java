package com.example.busbookingmanagement.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class VnPayUtil {

    /**
     * Build VNPay payment URL with secure hash
     */
    public static String buildVnpayUrl(String baseUrl, Map<String, String> params, String secretKey) {
        // 1. Sort params by key ASCII order
        Map<String, String> sorted = new TreeMap<>(params);

        // 2. Build hashData for secure hash (URL-encoded values)
        String hashData = sorted.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        // 3. Generate HMAC SHA512 hash
        String secureHash = hmacSHA512(secretKey, hashData);

        // 4. Build query string for URL (encode values again for URL)
        String query = sorted.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        String paymentUrl = baseUrl + "?" + query
                + "&vnp_SecureHashType=HmacSHA512"
                + "&vnp_SecureHash=" + secureHash;

        // Debug info
        System.out.println("============= VNPAY BUILD DEBUG =============");
        System.out.println("Sorted keys = " + sorted.keySet());
        System.out.println("[DEBUG] hashData BEFORE HASH = " + hashData);
        System.out.println("[DEBUG] secureHash GENERATED = " + secureHash);
        System.out.println(secretKey);
        System.out.println("[DEBUG] secretKey.length = " + (secretKey != null ? secretKey.length() : "null"));
        System.out.println("=============================================");

        return paymentUrl;
    }

    /**
     * Verify VNPay secure hash
     */
    public static boolean verifySecureHash(Map<String, String> params, String secretKey) {
        if (params == null || params.isEmpty()) return false;

        String provided = params.get("vnp_SecureHash");
        if (provided == null) return false;

        // Remove vnp_SecureHash & vnp_SecureHashType
        Map<String, String> copy = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            if (!k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")) {
                copy.put(k, e.getValue());
            }
        }

        // Build hashData for verification
        String hashData = copy.entrySet().stream()
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));

        String expected = hmacSHA512(secretKey, hashData);

        // Debug info
        System.out.println("============= VNPAY VERIFY DEBUG =============");
        System.out.println("[DEBUG] params keys = " + copy.keySet());
        System.out.println("[DEBUG] hashData = " + hashData);
        System.out.println("[DEBUG] expected = " + expected);
        System.out.println("[DEBUG] provided = " + provided);
        System.out.println("[DEBUG] secret length = " + (secretKey != null ? secretKey.length() : "null"));
        System.out.println("==============================================");

        return expected.equalsIgnoreCase(provided);
    }

    // URL encode helper
    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20"); // replace spaces with %20
        } catch (Exception e) {
            return s;
        }
    }

    // HMAC SHA512 helper
    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
