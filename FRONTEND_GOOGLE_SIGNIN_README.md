README - Hướng dẫn tích hợp Google Sign-In (Frontend -> Backend)

Mục đích

Tài liệu này hướng dẫn chi tiết cho frontend (React SPA chạy tại http://localhost:3000) để hoàn thiện chức năng "Đăng nhập bằng Google" theo luồng A: frontend nhận id_token từ Google Identity Services (GSI), gửi id_token về backend (POST /api/auth/google), nhận JWT ứng dụng từ backend, lưu và dùng JWT để gọi API được bảo vệ.

Checklist (những việc frontend dev cần hoàn thành)

- [ ] Đặt biến môi trường frontend: REACT_APP_GOOGLE_CLIENT_ID và REACT_APP_API_BASE
- [ ] Đảm bảo Google Cloud Console có `Authorized JavaScript origins` chứa http://localhost:3000
- [ ] Khi user click nút Google hoặc One‑Tap, frontend phải nhận `response.credential` (id_token)
- [ ] Gửi `POST {API_BASE}/api/auth/google` với payload JSON { "idToken": "<id_token>" }
- [ ] Khi backend trả về 200 + token: lưu token (localStorage hoặc state), set header Authorization cho axios/fetch
- [ ] Gọi GET /api/auth/profile để lấy thông tin user và cập nhật UI
- [ ] Xử lý lỗi (400/401/500) và hiển thị thông báo rõ ràng

Yêu cầu môi trường & cấu hình

1) Google Cloud Console
- OAuth client type = Web application
- Authorized JavaScript origins:
  - http://localhost:3000
  - (tùy chọn) http://localhost:5173 nếu dùng Vite
- (Redirect URIs không bắt buộc cho GSI/lấy id_token trực tiếp)

2) Biến môi trường frontend
- REACT_APP_GOOGLE_CLIENT_ID: client id copy từ Google Cloud Console
- REACT_APP_API_BASE: ví dụ http://localhost:8080

3) Backend
- Backend phải có `google.clientId` (application.properties hoặc env GOOGLE_CLIENT_ID) trùng với REACT_APP_GOOGLE_CLIENT_ID
- Endpoint POST /api/auth/google phải hoạt động (backend đã implement trong repo)

API contract (bắt buộc tuân theo)

- Endpoint: POST {API_BASE}/api/auth/google
  - Request header: Content-Type: application/json
  - Request body JSON: { "idToken": "<google-id-token>" }
  - Successful response (200):
    {
      "token": "<app-jwt>",
      "user": { "fullName": "...", "phone": "...", "role": "..." }
    }
  - Error responses:
    - 400: { "error": "Missing idToken" }
    - 401: { "error": "Invalid token" } or { "error": "Invalid audience" } or { "error": "Email not verified or missing" }
    - 500: server error

Các bước tích hợp frontend (tổng quan)

1) Thêm script Google Identity trong `public/index.html`:

```html
<script src="https://accounts.google.com/gsi/client" async defer></script>
```

2) Khởi tạo GSI ở component nơi cần nút đăng nhập (ví dụ khi mount):
- Gọi `google.accounts.id.initialize({ client_id: REACT_APP_GOOGLE_CLIENT_ID, callback: handleCredentialResponse })`
- Hiển thị nút: `google.accounts.id.renderButton(...)`
- (Tùy chọn) Call `google.accounts.id.prompt()` để hiển thị One‑Tap

3) Khi callback được gọi => `response.credential` là id_token
- Lấy `const idToken = response.credential`
- Gọi POST tới backend:

```js
// ví dụ dùng axios
axios.post(`${API_BASE}/api/auth/google`, { idToken })
  .then(res => {
    const appJwt = res.data.token; // lấy token trả về
    localStorage.setItem('app_jwt', appJwt); // hoặc lưu vào state/secure storage
    axios.defaults.headers.common['Authorization'] = `Bearer ${appJwt}`;
    // gọi GET /api/auth/profile để lấy user info
  })
  .catch(err => {
    // xử lý lỗi 400/401/500
  });
```

4) Sau khi lưu token: gọi GET {API_BASE}/api/auth/profile (Authorization header yêu cầu) để lấy thông tin user và cập nhật UI.

Quy tắc lưu token (gợi ý)

- Tốt nhất: lưu token ở memory (React state / context) để giảm nguy cơ XSS; nếu cần persist qua reload, có thể dùng localStorage hoặc sessionStorage nhưng hiểu rủi ro XSS.
- Nếu muốn an toàn hơn: yêu cầu backend trả token qua HTTP-only cookie (thay đổi backend cần thiết).

Auto-refresh token

- Backend có endpoint POST /api/auth/refresh (yêu cầu Authorization header hiện tại). Frontend nên gọi refresh trước khi token hết hạn (decode token, lấy claim `exp`) hoặc khi nhận 401 từ API.
- Có thể dùng `jwt-decode` để đọc `exp` và đặt timer refresh.

Kiểm thử (manual)

1) Đảm bảo backend đang chạy và `google.clientId` trong backend trùng với REACT_APP_GOOGLE_CLIENT_ID
2) Khởi động frontend (`npm start` / `yarn dev`) ở http://localhost:3000
3) Mở DevTools -> Console và Network
4) Click Google Sign-in button -> trong callback console.log(response) -> kiểm tra `response.credential` tồn tại
5) Kiểm tra request POST /api/auth/google: body có idToken, response 200 chứa token
6) Sau lưu token, kiểm tra GET /api/auth/profile trả về user info

Các lỗi phổ biến và cách xử lý

- The given origin is not allowed for the given client ID
  - Nguyên nhân: thiếu `http://localhost:3000` trong Authorized JavaScript origins trên Google Cloud.
  - Fix: thêm chính xác origin (bao gồm port).

- Invalid audience
  - Nguyên nhân: frontend dùng client id khác so với backend `google.clientId`.
  - Fix: set REACT_APP_GOOGLE_CLIENT_ID = Google client id; set backend GOOGLE_CLIENT_ID trùng.

- CORS preflight blocked / No 'Access-Control-Allow-Origin'
  - Nguyên nhân: backend không allow origin hoặc config CORS chưa đúng.
  - Fix: đảm bảo backend `SecurityConfig.corsConfigurationSource()` có `http://localhost:3000` trong allowedOrigins (repo đã cấu hình).

- 500 server error when creating user
  - Nguyên nhân có thể DB constraints (password null) hoặc lỗi repo.
  - Fix: xem server logs; backend đã được cập nhật để tạo password ngẫu nhiên mã hóa khi tạo user từ Google.

Tiêu chí nghiệm thu (Acceptance)

- Khi click nút Google, frontend nhận `response.credential` và gửi tới backend.
- Backend trả 200 với `token` (JWT ứng dụng) và `user` info.
- Frontend lưu token và set Authorization header; gọi API bảo vệ (ví dụ GET /api/auth/profile) trả về dữ liệu.
- Không còn lỗi "origin not allowed" hoặc "Invalid audience".

Bảo mật & production notes

- KHÔNG commit `JWT_SECRET` hay `client_secret` vào repo.
- Trong production: dùng HTTPS, và prefer server-side verification with Google's JWKS (mình có thể implement nếu muốn).
- Nếu muốn refresh token dài hạn cho SPA: dùng Authorization Code + PKCE hoặc lưu refresh token ở server side.

Nếu cần mẫu component React hoàn chỉnh hoặc hướng dẫn từng bước config Google Cloud Console (có ảnh chụp màn hình), phản hồi "Gửi code mẫu" hoặc "Hướng dẫn Google Console" và tôi sẽ bổ sung.

