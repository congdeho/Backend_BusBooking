# Google Sign-In (id_token) Backend Setup - BusBooking

This file explains the minimal setup to run Google Sign-In (frontend obtains id_token, backend verifies it) with the current Spring Boot backend.

Quick checklist

- Set environment variables:
  - `GOOGLE_CLIENT_ID` — the OAuth client ID from Google Cloud Console (Web application)
  - `JWT_SECRET` — a strong secret (at least 32 characters) used for signing HS256 tokens
  - Optional: `JWT_EXPIRATION_MS` in milliseconds (default 86400000 = 1 day)

- Start backend (Maven):
  - `mvn spring-boot:run` or build `mvn clean package -DskipTests` then run jar

Backend endpoints implemented

- POST /api/auth/google
  - Request: `{ "idToken": "<google-id-token>" }`
  - Behavior:
    - Call `https://oauth2.googleapis.com/tokeninfo?id_token=<idToken>` to validate the idToken
    - Verify `aud` equals `google.clientId` (from `application.properties` or env)
    - Verify `email_verified` == true and `email` exists
    - If user exists in DB (by email) -> use it
    - If user not exists -> create user with role `CUSTOMER` (role created if missing), set a random encoded password
    - Generate application JWT (HS256) whose subject = userId and claims include `name`, `email`, `role`
    - Response: `{ "token": "<app-jwt>", "user": { "fullName": ..., "email": ..., "phone": ..., "role": ... } }`

- POST /api/auth/login
  - Standard email/password login, returns JWT with same claims

- GET /api/auth/profile
  - Requires Authorization: Bearer <jwt>
  - Returns user profile JSON

- POST /api/auth/refresh
  - Requires Authorization: Bearer <jwt>
  - Returns a fresh token in `{ "token": "..." }`

Notes about `client_secret`

- For this id_token verification flow you do NOT need a `client_secret` on the backend.
- A `client_secret` is required only if you perform an Authorization Code Flow and the server exchanges an authorization code for tokens.

Frontend integration (minimal)

1. Acquire `id_token` using Google Identity Services (GSI) or One-Tap on the frontend. Configure GSI with the same `GOOGLE_CLIENT_ID` and ensure "Authorized JavaScript origins" includes your front-end origin (e.g. http://localhost:3000).

2. Send `id_token` to backend:

```js
// example using fetch/axios
axios.post('http://localhost:8080/api/auth/google', { idToken: credential })
  .then(res => {
    // save res.data.token
    localStorage.setItem('jwt', res.data.token);
  })
  .catch(err => console.error(err));
```

3. For protected API calls, include Authorization header:

```
Authorization: Bearer <jwt>
```

Security & production notes

- Do not commit `JWT_SECRET` to source control.
- For production, prefer verifying tokens locally using Google's public keys (jwks) rather than calling tokeninfo.
- Use HTTPS in production.
- Consider refresh tokens / blacklisting if you need logout.

If you want, I can:
- Implement local verification using Google's JWKS (no external request to tokeninfo).
- Add an endpoint to exchange authorization code (server-side OAuth code flow) that requires CLIENT_SECRET.
- Provide example React code to get id_token with Google Identity Services.

