# Phase 1: Real Authentication, Email OTP & Forgot Password Report

## 1. Executive Summary

Phase 1 establishes a comprehensive, production-grade real authentication system for LearnAI across the Spring Boot backend and React + TypeScript `frontend-v2` application. All mock authentication has been replaced with cryptographically secure flows, BCrypt password hashing, JWT token handling, rate-limited email OTP verification, and secure password resets.

---

## 2. Security & Environment Architecture

### 2.1 Secrets Isolation
- **Backend-Only Secrets**: All sensitive keys and credentials (JWT secret, SMTP server credentials, PostgreSQL/Supabase database credentials, Gemini API key) are strictly contained within backend environment variables (`.env`).
- **Zero Frontend Secret Leaks**: The frontend interacts exclusively via standard REST endpoints (`/api/auth/*`) with Bearer tokens.

### 2.2 Security Policies & Rate Limiting
- **OTP Generation**: Cryptographically secure 6-digit random codes generated using `java.security.SecureRandom`.
- **OTP Persistence**: Hashed with `BCryptPasswordEncoder` prior to database storage.
- **OTP Expiration**: Default 10 minutes (`OTP_EXPIRY_MINUTES=10`).
- **Resend Cooldown**: 60 seconds rate limit (`OTP_RESEND_COOLDOWN_SECONDS=60`) preventing OTP flooding.
- **Attempt Limiting**: Max 5 attempts (`OTP_MAX_ATTEMPTS=5`). Reaching the maximum attempts immediately invalidates the OTP.
- **Anti-Enumeration**: The `/forgot-password` endpoint returns a generic success response regardless of whether the email is registered.
- **Account Verification Enforcement**: Users are created with `email_verified = false`. Unverified accounts cannot log in and are routed to `/verify-email` with an `EMAIL_NOT_VERIFIED` code.

---

## 3. Backend Architecture

### 3.1 Entity & Schema
- **`User`** (`com.learningpath.entity.User`):
  - Added `@Column(name = "email_verified", nullable = false) private boolean emailVerified = false;`
- **`OtpVerification`** (`com.learningpath.entity.OtpVerification`):
  - `id` (UUID), `email` (String), `otpHash` (String), `purpose` (`OtpPurpose`: `EMAIL_VERIFICATION` | `PASSWORD_RESET`), `expiresAt` (Instant), `attemptCount` (int), `maxAttempts` (int), `used` (boolean), `createdAt` (Instant), `verifiedAt` (Instant).
  - Database indexes on `(email, purpose)` and `expires_at`.

### 3.2 Services & Security
- **`EmailService`** (`com.learningpath.service.EmailService`):
  - Delivers HTML & plaintext branded emails via Spring `JavaMailSender`.
  - Development logger fallback logs OTPs to console (`[EMAIL SERVICE] OTP for email=... is: [123456]`).
- **`OtpService`** (`com.learningpath.service.OtpService`):
  - Handles secure random code generation, hashing, rate limiting, attempt counters, expiration checks, and invalidation.
- **`AuthService`** (`com.learningpath.service.AuthService`):
  - `signup(SignupRequest)`: Hashes password, saves user with `emailVerified = false`, dispatches verification OTP.
  - `verifyEmailOtp(VerifyEmailOtpRequest)`: Verifies OTP, updates `user.emailVerified = true`, returns JWT `AuthResponse`.
  - `resendOtp(ResendOtpRequest)`: Enforces 60s cooldown, invalidates prior active OTPs, sends new OTP.
  - `login(LoginRequest)`: Validates credentials and verifies `user.isEmailVerified()`.
  - `forgotPassword(ForgotPasswordRequest)`: Dispatches `PASSWORD_RESET` OTP for valid users with anti-enumeration protection.
  - `verifyResetOtp(VerifyResetOtpRequest)`: Verifies reset OTP, generates short-lived (15-min) JWT `resetToken`.
  - `resetPassword(ResetPasswordRequest)`: Validates `resetToken`, password strength, and updates hashed password.
- **`JwtService`** (`com.learningpath.security.JwtService`):
  - Added support for generating and validating dedicated password reset tokens with purpose claim checks.
- **`SecurityConfig`** (`com.learningpath.security.SecurityConfig`):
  - Configured public access to all `/api/auth/*` endpoints (except `/api/auth/me` which requires JWT Bearer auth).

---

## 4. Frontend Architecture (`frontend-v2/`)

### 4.1 API Client & DTOs (`src/api/`)
- `client.ts`:
  - `api.signup({ name, email, password })`
  - `api.verifyEmailOtp({ email, otp })`
  - `api.resendOtp({ email, purpose })`
  - `api.login({ email, password })`
  - `api.forgotPassword({ email })`
  - `api.verifyResetOtp({ email, otp })`
  - `api.resetPassword({ resetToken, newPassword, confirmPassword })`

### 4.2 UI & User Flows
1. **Signup Flow** (`/signup` -> `SignupForm.tsx`):
   - Client-side validation + password strength indicator.
   - On submit, calls `api.signup()` and redirects to `/verify-email` with `{ email, purpose: 'EMAIL_VERIFICATION' }`.
2. **Email OTP Verification** (`/verify-email` -> `VerifyEmailPage.tsx`):
   - 6-digit interactive OTP boxes with auto-focus and paste support.
   - 60s resend cooldown timer.
   - On success, automatically stores token, refreshes user context, and redirects to platform dashboard.
3. **Login Verification Enforcement** (`/login` -> `LoginPage.tsx`):
   - If user email is unverified, gracefully detects `EMAIL_NOT_VERIFIED` and redirects to `/verify-email`.
4. **Forgot Password Flow** (`/forgot-password` -> `ForgotPasswordPage.tsx`):
   - Submits email to `api.forgotPassword()` and redirects to `/verify-email` with `{ email, purpose: 'PASSWORD_RESET' }`.
5. **Reset Password Flow** (`/reset-password` -> `ResetPasswordPage.tsx`):
   - Reads `resetToken` from navigation state or URL query parameter.
   - Validates live password strength and matching.
   - Calls `api.resetPassword()` and navigates to `/password-reset-success`.

---

## 5. Verification Results

| Verification Step | Target | Status |
| :--- | :--- | :--- |
| Backend Maven Compilation | Java 17, Spring Boot 4.1.0-compatible | **PASSED (0 Errors)** |
| Frontend Vite & TypeScript Build | `tsc && vite build` in `frontend-v2/` | **PASSED (0 Errors)** |
| Public Security Filter Chain | `/api/auth/**` permitted, `/api/auth/me` secured | **PASSED** |
| Environment Config Documentation | `backend/learning-path-backend/.env.example` | **PASSED** |
