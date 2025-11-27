# Tài liệu API tổng hợp — Backend BusBooking (Tiếng Việt)

Phiên bản tài liệu: 1.1
Base URL (local dev): `http://localhost:8080`

Tổng quan
- Tài liệu này là bản tổng hợp các endpoint hiện có của backend BusBooking. Bao gồm mô tả endpoint, phương thức, auth/role yêu cầu, tham số chính, mẫu request/response và các chú ý vận hành (CORS, JWT, VNPAY, WebSocket).
- Header mặc định: `Content-Type: application/json` và `Accept: application/json`.
- Endpoint yêu cầu xác thực phải đính kèm header: `Authorization: Bearer <JWT>`.
- Định dạng thời gian: ISO-8601 (ví dụ `2025-11-19T08:00:00`).

Định dạng lỗi chuẩn (ví dụ):
```
{
  "timestamp": "2025-11-21T12:00:00.000+07:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: ...",
  "path": "/api/..."
}
```

Chú ý ngữ nghĩa role/authority
- DB lưu role như: `ADMIN`, `STAFF`, `CUSTOMER`.
- Trong Spring Security, `hasRole("ADMIN")` kiểm tra authority `ROLE_ADMIN`.
- Hệ thống hiện cấp cả `ROLE_{NAME}` và `{NAME}` vào principal lúc xác thực để tránh mismatch (cả `ROLE_ADMIN` và `ADMIN`).

=== 1) AUTH ===

1.1 POST /api/auth/login
- Mô tả: Đăng nhập bằng email/password.
- Auth: Không
- Request:
```
{ "email": "admin@example.com", "password": "123456" }
```
- Response 200:
```
{
  "token": "<JWT>",
  "user": { "userId":1, "fullName":"Nguyễn Văn A", "email":"admin@example.com", "role":"ADMIN" }
}
```
- Lỗi: 400 (thiếu param), 401 (invalid credentials), 403 (account inactive)

1.2 POST /api/auth/register
- Mô tả: Đăng ký user mới (public register)
- Auth: Không
- Request mẫu:
```
{ "fullName":"X", "email":"x@example.com", "password":"123456", "phone":"090..." }
```
- Response 200: `AuthResponse` giống login

1.3 POST /api/auth/google
- Mô tả: Đăng nhập/đăng ký qua Google ID token
- Auth: Không
- Request:
```
{ "idToken": "<google-id-token>" }
```
- Flow (tóm tắt):
  1. Backend gọi `https://oauth2.googleapis.com/tokeninfo?id_token=<idToken>` để verify.
  2. Kiểm tra `aud` === `GOOGLE_CLIENT_ID` cấu hình trên server và `email_verified` là true.
  3. Nếu user không tồn tại -> tạo user mới (role mặc định `CUSTOMER`, tạo password ngẫu nhiên/hashed).
  4. Sinh JWT và trả về `token` + `user` info.

1.4 GET /api/auth/profile
- Mô tả: Lấy profile user đang đăng nhập
- Auth: Có
- Response 200 mẫu:
```
{ "userId":1, "fullName":"...", "email":"...", "phone":"...", "role":"ADMIN" }
```

1.5 POST /api/auth/refresh
- Mô tả: Tạo token mới (nếu sử dụng refresh tokens)
- Auth: Có
- Response 200: `{ "token": "<newToken>" }`


=== 2) USERS ===

2.1 GET /api/users/me
- Mô tả: Lấy profile hiện tại
- Auth: Có

2.2 PUT /api/users/me
- Mô tả: Cập nhật profile
- Auth: Có
- Request mẫu:
```
{ "fullName":"Tên mới", "phone":"090...", "password":"newpass" }
```
- Lưu ý: Nếu thay password, backend phải hash (BCrypt).

2.3 GET /api/users
- Mô tả: Lấy danh sách users
- Auth: ADMIN
- Query params: page, size, role, status, q (search)

2.4 POST /api/users
- Mô tả: Tạo user (Admin create)
- Auth: ADMIN


=== 3) ROLES ===

GET /api/roles
- Mô tả: Danh sách roles
- Auth: Không


=== 4) BUSES ===

4.1 GET /api/buses
- Mô tả: Lấy danh sách bus (có thể filter)
- Query: page,size,busType,driverId,status
- Auth: Không (public read)

4.2 GET /api/buses/{id}
- Mô tả: Chi tiết bus
- Auth: Không

4.3 POST /api/buses
4.4 PUT /api/buses/{id}
4.5 DELETE /api/buses/{id}
- Mô tả: CRUD buses
- Auth: ADMIN


=== 5) DRIVERS ===
- Endpoint CRUD tương tự BUSES (ADMIN)


=== 6) ROUTES ===

GET /api/routes
GET /api/routes/{id}
POST/PUT/DELETE (ADMIN)
- Route object sample:
```
{ "routeId":1, "startPoint":"TP.HCM", "endPoint":"Đà Lạt", "distanceKm":320.00, "basePrice":300000.00 }
```


=== 7) SCHEDULES ===

7.1 GET /api/schedules
- Query: routeId,date,busId,status,page,size
- Auth: public read

7.2 GET /api/schedules/{id}
7.3 POST/PUT/DELETE /api/schedules (ADMIN)
- POST sample:
```
{ "routeId":1, "busId":1, "departureTime":"2025-12-01T08:00:00Z", "arrivalTime":"2025-12-01T14:00:00Z", "availableSeats":40 }
```


=== 8) SEATS ===

8.1 GET /api/seats/schedule/{scheduleId}
- Mô tả: Lấy map ghế cho chuyến. Trả tất cả trạng thái (AVAILABLE, BOOKED, MAINTENANCE) — frontend có thể hiển thị map đầy đủ.
- Auth: Không

8.2 GET /api/seats/bus/{busId}


=== 9) BOOKINGS ===

9.1 POST /api/bookings
- Mô tả: Tạo booking
- Auth: Có (USER)
- Request example:
```
{
  "scheduleId":1,
  "passengers":[ { "fullName":"Nguyễn A","phone":"090...","seatNumber":"A1","price":300000 } ],
  "promotionCode":"SALE10",
  "paymentMethod":"VNPAY"
}
```
- Response 201 sample:
```
{ "bookingId":1, "status":"PENDING", "totalAmount":300000, "discountAmount":30000, "finalAmount":270000 }
```
- Business rules: kiểm tra seat availability (khoá hàng trong transaction), nếu confirmed -> tạo tickets, mark seat BOOKED.
- Lỗi: 400 (seat already booked), 404 (schedule not found)

9.2 GET /api/bookings (user)
9.3 GET /api/bookings/{id}
9.4 POST /api/bookings/{id}/cancel
- Auth: authenticated owner or admin as appropriate


=== 10) PAYMENTS ===

10.1 POST /api/payments
- Mô tả: Tạo Payment record hoặc khởi tạo payment URL VNPAY
- Auth: Có
- Request sample:
```
{ "bookingId":1, "method":"VNPAY", "amount":270000 }
```
- Response: `{ "paymentId":456, "status":"PENDING", "paymentUrl":"<vnpay-url>" }`

10.2 POST /api/payments/callback (gateway -> backend)
- Mô tả: Endpoint nhận callback từ VNPAY (public)
- Thực hiện: verify secure hash (vnp_SecureHash) -> tạo Payment (SUCCESS/FAILED), update Booking status, emit notification, tạo Invoice.
- Endpoint public examples in code: `/api/payment/vnpay/confirm` and `/api/payments/vnpay/confirm` (cả hai được permitAll để tương thích với cấu hình gateway).
- Lưu ý: Khi tạo `Payment` qua builder, `paymentTime` phải được set (mặc định builder đã dùng `@Builder.Default LocalDateTime.now()`), và `status` phải được set.


=== 11) PROMOTIONS ===
- GET /api/promotions
- GET /api/promotions/{code}
- Admin CRUD endpoints (ADMIN)


=== 12) INVOICES ===
- GET /api/invoices/{id} (owner/admin) -> trả invoice JSON hoặc link PDF


=== 13) NOTIFICATIONS ===
- GET /api/notifications (user)
- POST /api/notifications (system/admin)
- Notification types: EMAIL, PUSH, etc.


=== 14) TICKETS ===
- GET /api/tickets/booking/{bookingId} (owner) -> list tickets + qrCode


=== 15) ADMIN ENDPOINTS ===
- GET /api/admin/overview (ADMIN) — thống kê tổng quát: tổng users/bookings/payments/tickets/buses, recent bookings/payments/users, revenue series, top schedules
- GET /api/admin/bookings (ADMIN)
- CRUD endpoints cho quản trị: buses/drivers/routes/schedules/users/promotions
- Security: `SecurityConfig` hiện yêu cầu `.requestMatchers("/api/admin/**").hasRole("ADMIN")` — tức principal cần authority `ROLE_ADMIN` (hệ thống cấp cả `ROLE_ADMIN` và `ADMIN` khi xác thực JWT)


=== 16) SWAGGER / OPENAPI ===
- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui/index.html` hoặc `/swagger-ui.html`
- Cần permitAll cho `/v3/api-docs/**` và `/swagger-ui/**` trong `SecurityConfig` để UI load được khi auth bật.


=== 17) WEBSOCKET / SEAT LOCKING ===
- WS endpoint(s): `/ws` (handshake) và STOMP controller tại `/ws/seats/**` — dùng để broadcast seat lock/unlock events trong thời gian người dùng chọn ghế.
- WS handshake được permitAll trong security config.


=== 18) CORS & DEPLOY NOTES ===
- Backend CORS config uses explicit allowed origins (do not use `*` when `allowCredentials=true`). Include dev origins:
  - `http://localhost:3000` (React) and `http://localhost:5173` (Vite) etc.
- Ensure `Access-Control-Expose-Headers` includes `Authorization` if you expect backend to set tokens in response header.
- In `SecurityConfig`, CORS bean is picked up via `cors(c -> c.configurationSource(corsConfigurationSource))`.
- If frontend sees Authorization missing in network calls, check browser preflight and server's `Access-Control-Allow-Headers`/`Expose-Headers`.


=== 19) JWT / AUTHENTICATION NOTES ===
- JWT created by backend includes claims: subject = userId (string), `email`, `name`, `role`.
- `JwtAuthenticationFilter` extracts `userId` or `email` from token, loads `User` from DB, validates token and then sets authentication.
- The filter constructs UserDetails with authorities both `ROLE_{NAME}` and `{NAME}` to avoid mismatch between DB roleName and Spring `hasRole()` checks.
- Always send `Authorization: Bearer <token>` on protected endpoints.


=== 20) EXAMPLES (curl & axios) ===
- Login (curl):
```
curl -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d '{"email":"admin@example.com","password":"123456"}'
```

- Axios default header example:
```js
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
// or per-request
axios.get('/api/admin/overview', { headers: { Authorization: `Bearer ${token}` } });
```

- Simulate VNPAY callback (curl):
```
curl -X POST "http://localhost:8080/api/payments/vnpay/confirm" -d 'vnp_TxnRef=1&vnp_Amount=27000000&vnp_ResponseCode=00&vnp_TransactionNo=12345&vnp_SecureHash=...'
```


=== 21) COMMON ERRORS & TROUBLESHOOTING ===
- 401 Unauthorized: missing/invalid token or token expired. Verify token present and correct signing secret `app.jwt.secret`.
- 403 Forbidden: principal lacks required role authority. Verify JWT contains correct role claim and Authentication grants `ROLE_{NAME}` or use proper `hasAuthority` checks. Also check SecurityConfig permit rules for public endpoints.
- 500 NullPointerException when reading `paymentTime`: means some Payment rows had null `paymentTime`. Fixes:
  - Ensure code sets `paymentTime` when creating Payment (we set `.paymentTime(LocalDateTime.now())`).
  - Add `@Builder.Default` to `paymentTime` in `Payment` entity so Lombok builder fills default.
  - Backfill existing DB rows with null payment_time if necessary.
- Swagger UI `Failed to load remote configuration`: check `/v3/api-docs` response and CORS / security rules permitting access.
- VNPAY signature verification failures: ensure `vnp_SecureHash` is computed with the same sorted param encoding and HMAC-SHA512 secret as configured in `vnpay.hashSecret`.


=== 22) MAINTENANCE / DATA-FIX SUGGESTIONS ===
- Backfill payments missing `payment_time`:
  - Run SQL: `UPDATE payments SET payment_time = NOW() WHERE payment_time IS NULL;` (or a safer migration with business logic)
- Normalize roles: consider storing `role_name` without `ROLE_` prefix and adding prefix only when granting authorities, or add DB migration to add `ROLE_` prefix consistently.


=== 23) NEXT STEPS I CAN HELP WITH ===
Chọn 1 trong các việc sau, tôi sẽ thực hiện:
- A) Tạo OpenAPI (YAML) skeleton tự động dựa trên tài liệu này.
- B) Cập nhật `CorsConfig` / `SecurityConfig` để mở rộng origin dev / tạm permitAll endpoints cho test.
- C) Tạo script/migration để backfill `payment_time` cho các bản ghi hiện có.
- D) Sinh file markdown `API_DOCUMENTATION.md` chính thức (tôi đã cập nhật file này) và commit.


Nếu bạn muốn một trong các tùy chọn trên, trả lời chữ cái lựa chọn (A/B/C/D) hoặc cho biết chỉnh sửa cụ thể khác bạn muốn. Cần tôi chạy build/start server để test endpoint nào không? Hãy cho biết endpoint và tôi sẽ chạy kiểm tra log / request mẫu.
