# Tài liệu API - Backend BusBooking (Tiếng Việt)

Phiên bản: 1.0

Mô tả: Tài liệu này mô tả các endpoint backend (module Auth, Users, Buses, Routes, Schedules, Seats, Bookings, Payments, Promotions, Invoices, Notifications, Tickets, Admin), kèm mẫu request/response, lỗi thường gặp, và hướng dẫn sử dụng JWT/CORS.

Base URL (local dev): `http://localhost:8080`

Tiêu chí trả về lỗi chuẩn (ví dụ):
```
{
  "timestamp": "2025-11-21T12:00:00.000+07:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: ...",
  "path": "/api/..."
}
```

---

MỘT SỐ LƯU Ý CHUNG
- Header chung: `Content-Type: application/json`, `Accept: application/json`.
- Các endpoint yêu cầu xác thực phải gửi header: `Authorization: Bearer <JWT>`.
- Ngày giờ: ISO-8601 (ví dụ `2025-11-19T08:00:00Z`).
- Trường mapping DB -> JSON: `user_id` -> `userId`, `full_name` -> `fullName`, `created_at` -> `createdAt`.
- Mật khẩu KHÔNG bao giờ trả về cho frontend.

---

1) AUTH

1.1 POST /api/auth/login
- Mô tả: Đăng nhập bằng email/password.
- Auth: Không
- Request:
```json
{ "email": "admin@example.com", "password": "123456" }
```
- Response 200:
```json
{
  "token": "<JWT>",
  "user": { "userId":1, "fullName":"Nguyễn Văn A", "email":"admin@example.com", "role":"ADMIN" }
}
```
- Lỗi: 400 (thiếu param), 401 (invalid credentials), 403 (account inactive).
- Curl:
```
curl -X POST "http://localhost:8080/api/auth/login" -H "Content-Type: application/json" -d '{"email":"admin@example.com","password":"123456"}'
```

1.2 POST /api/auth/google
- Mô tả: Đăng nhập/Đăng ký qua Google id_token.
- Auth: Không
- Request:
```json
{ "idToken": "<google-id-token>" }
```
- Flow backend (tóm tắt):
  1. Gọi `https://oauth2.googleapis.com/tokeninfo?id_token=<idToken>`.
  2. Kiểm tra `aud` === `GOOGLE_CLIENT_ID` (cấu hình trên server).
  3. Kiểm tra `email` tồn tại và `email_verified` = true.
  4. Tìm user theo email (UserService.findByEmail). Nếu không tồn tại -> tạo user role = CUSTOMER (UserService.create).
  5. Sinh JWT nội bộ và trả về.
- Response 200:
```json
{ "token":"<JWT>", "user": {"userId":12, "fullName":"...", "email":"...", "role":"CUSTOMER"} }
```
- Lỗi: 400 (missing idToken), 401 (aud mismatch / token invalid / email_verified != true).

1.3 POST /api/auth/refresh
- Mô tả: Lấy access token mới từ refresh token (nếu app dùng refresh tokens).
- Auth: Không (gửi refresh token trong body)
- Request:
```json
{ "refreshToken": "<refresh>" }
```
- Response 200: `{ "token": "<newAccessToken>" }`

1.4 POST /api/auth/logout
- Mô tả: Đăng xuất; optional blacklist token.
- Auth: Có
- Response: `{ "message": "Logged out" }`

---

2) USERS

2.1 GET /api/users/me
- Mô tả: Lấy profile user hiện tại.
- Auth: Có
- Response 200:
```json
{
  "userId":3,
  "fullName":"Lê Minh C",
  "email":"customer1@example.com",
  "phone":"0909345678",
  "role":"CUSTOMER",
  "status":"ACTIVE",
  "createdAt":"2025-11-05T01:08:43"
}
```

2.2 PUT /api/users/me
- Mô tả: Cập nhật profile hiện tại.
- Auth: Có
- Request mẫu (có thể chỉ một số trường):
```json
{ "fullName":"Tên mới", "phone":"0909...", "password":"newpass" }
```
- Lưu ý: Nếu thay password, server phải hash (bcrypt) trước khi lưu.

2.3 GET /api/users (ADMIN)
- Mô tả: Lấy danh sách users
- Auth: Có; role ADMIN
- Query: page,size,role,status,q (search)

2.4 POST /api/users (register or admin create)
- Mô tả: Tạo user mới
- Auth: có thể không (đăng ký) hoặc ADMIN
- Request:
```json
{ "fullName":"X", "email":"x@example.com", "password":"123456", "phone":"090...", "role":"CUSTOMER" }
```
- Response 201: created user (không trả password)

---

3) ROLES

GET /api/roles
- Mô tả: Lấy role list.
- Auth: Không/Optional
- Response:
```json
[{ "roleId":1, "roleName":"ADMIN", "description":"..." }, ...]
```

---

4) BUSES

4.1 GET /api/buses
- Mô tả: Lấy list buses (filterable)
- Query: page,size,busType,driverId,status
- Response: page of bus objects

4.2 GET /api/buses/{id}
- Mô tả: Chi tiết bus

4.3 POST/PUT/DELETE /api/buses (ADMIN)
- Auth: ADMIN
- Request mẫu POST:
```json
{ "licensePlate":"51B-12345","busType":"Giường nằm 40 chỗ","totalSeats":40,"driverId":1 }
```

---

5) DRIVERS
- Các endpoint CRUD tương tự buses; Admin chịu trách nhiệm tạo/sửa/xóa.

---

6) ROUTES
- GET /api/routes  (list)
- GET /api/routes/{id}
- POST/PUT/DELETE (ADMIN)

Route object example:
```json
{ "routeId":1, "startPoint":"TP.HCM", "endPoint":"Đà Lạt", "distanceKm":320.00, "basePrice":300000.00 }
```

---

7) SCHEDULES (CHUYẾN)

7.1 GET /api/schedules
- Query: routeId,date,busId,status,page,size
- Response: list of schedules

7.2 GET /api/schedules/{id}
7.3 POST/PUT/DELETE (ADMIN)
- POST sample:
```json
{ "routeId":1, "busId":1, "departureTime":"2025-12-01T08:00:00Z", "arrivalTime":"2025-12-01T14:00:00Z", "availableSeats":40 }
```

---

8) SEATS

8.1 GET /api/seats/schedule/{scheduleId}
- Mô tả: Lấy danh sách ghế của chuyến (tùy cài đặt: có thể trả tất cả bao gồm BOOKED/MAINTENANCE hoặc chỉ AVAILABLE). Nếu bạn muốn hiển thị cả BOOKED/MAINTENANCE, server phải trả tất cả trạng thái.
- Auth: Không
- Response sample:
```json
[
  { "seatId":2, "seatNumber":"A2", "status":"AVAILABLE" },
  { "seatId":4, "seatNumber":"A4", "status":"BOOKED" }
]
```
- Gợi ý: Nếu hiện code đang ẩn BOOKED/MAINTENANCE, sửa service để không filter theo status khi frontend cần toàn bộ seat map.

8.2 GET /api/seats/bus/{busId}

---

9) BOOKINGS

9.1 POST /api/bookings
- Mô tả: Tạo booking
- Auth: Có (USER)
- Request mẫu:
```json
{
  "scheduleId":1,
  "passengers":[ { "fullName":"Nguyễn A","phone":"090...","seatNumber":"A1","price":300000 } ],
  "promotionCode":"SALE10",
  "paymentMethod":"VNPAY"
}
```
- Response 201:
```json
{ "bookingId":1, "status":"PENDING", "totalAmount":300000, "discountAmount":30000, "finalAmount":270000 }
```
- Business rules: kiểm tra seat availability (transactional), nếu confirm -> tạo tickets, mark seat BOOKED.
- Lỗi: 400 (seat already booked), 404 (schedule not found).

9.2 GET /api/bookings (user)
9.3 GET /api/bookings/{id}
9.4 POST /api/bookings/{id}/cancel

---

10) PAYMENTS

10.1 POST /api/payments
- Mô tả: Tạo payment record hoặc khởi tạo thanh toán VNPAY.
- Auth: Có
- Request:
```json
{ "bookingId":1, "method":"VNPAY", "amount":270000 }
```
- Response: { "paymentId":456, "status":"PENDING", "paymentUrl":"..." }

10.2 POST /api/payments/callback (gateway -> backend)
- Cập nhật trạng thái payment (SUCCESS/FAILED)

---

11) PROMOTIONS
- GET /api/promotions
- GET /api/promotions/{code}

---

12) INVOICES
- GET /api/invoices/{id} (owner/admin) -> trả invoice JSON hoặc link PDF

---

13) NOTIFICATIONS
- GET /api/notifications (user)
- POST /api/notifications (system/admin)

---

14) TICKETS
- GET /api/tickets/booking/{bookingId} (owner) -> list tickets, qrCode

---

15) ADMIN ENDPOINTS (ví dụ)
- GET /api/admin/stats (ADMIN) -> tổng booking, revenue, activeBuses
- GET /api/admin/bookings
- CRUD buses/drivers/routes/schedules

---

16) SWAGGER / OPENAPI
- `GET /v3/api-docs` -> OpenAPI JSON
- `GET /swagger-ui/index.html` -> swagger UI
- Nếu gặp lỗi `Failed to load remote configuration` hoặc CORS: cần đảm bảo `/v3/api-docs/**` và `/swagger-ui/**` được permitAll và CORS cho phép origin frontend.

---

17) JWT & Authorization (Frontend hướng dẫn)
- Sau login, lưu `token` (LocalStorage/Memory). Mỗi request cần header:
  `Authorization: Bearer <token>`
- Axios example:
```js
axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
```

18) CORS notes (backend)
- Nếu `allowCredentials=true` thì `allowedOrigins` KHÔNG được là `*`.
- Thêm origin cụ thể: `http://localhost:5173`, `http://localhost:3000` (frontend dev) vào cấu hình CORS.
- Không set `Cross-Origin-Opener-Policy: same-origin` và `Cross-Origin-Embedder-Policy: require-corp` khi dùng Google One Tap/FedCM; dùng `unsafe-none` nếu cần dev.

---

19) Debug / Kiểm tra nhanh
- Để kiểm tra login:
  1. POST /api/auth/login -> nhận token
  2. GET /api/users/me với Authorization header -> trả profile
- Nếu 403 trên login: check SecurityConfig (có thể chặn endpoint `/api/auth/**`).
- Nếu swagger UI báo `Failed to load remote configuration`: kiểm tra `/v3/api-docs` có trả JSON không và CORS/CSRF/SecurityConfig có cho phép.
- Nếu Google login: log payload token (`aud`) ở frontend, log `expectedAudience` trên backend, so sánh.

---

20) Các lỗi thường gặp và cách khắc phục nhanh
- 401 Invalid credentials: kiểm tra hashing (bcrypt) và so sánh đúng; password lưu ở DB phải là hash bcrypt; khi test, mật khẩu plain text phải match sau encode.
- 403 Forbidden: kiểm tra SecurityConfig (permitAll cho /api/auth/**) và CORS settings.
- 500 Error when creating user from Google sign-in: kiểm tra UserService.create yêu cầu password NOT NULL. Nếu tạo user từ Google, cần gán `password` tạm (random/uuid) và hash lại, hoặc allow NULL trong DB and code.
- Swagger CORS: permit `/v3/api-docs/**` và `/swagger-ui/**` trong SecurityConfig and CorsConfig.

---

Nếu bạn muốn, tôi có thể tiếp tục và làm một trong các việc sau (chọn 1):
- A) Tạo file OpenAPI skeleton (YAML) tự động từ danh sách này.
- B) Sửa `CorsConfig` và `SecurityConfig` trong mã nguồn để tạm cho phép tất cả endpoint `permitAll` (dễ test), và thêm origin cụ thể cho CORS.
- C) Tạo một file `API_DOCUMENTATION.json` machine-readable (apiList) để frontend import.

Nhắn tôi lựa chọn bạn muốn tiếp theo, tôi sẽ thực hiện và chạy kiểm tra.

