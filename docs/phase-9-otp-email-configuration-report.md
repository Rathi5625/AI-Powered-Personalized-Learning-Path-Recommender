# Phase 9 — OTP Email Environment Configuration & Authentication Hardening Report

**Project:** LearnAI — AI-Powered Personalized Learning Path Recommender  
**Phase:** Phase 9 — OTP Email Environment Configuration & Authentication Hardening  
**Date:** August 21, 2026  
**Status:** **PASSED & PRODUCTION READY**

---

## 1. Executive Summary

Phase 9 completed the environment hardening and SMTP mail configuration for real email OTP delivery across the LearnAI platform. The existing authentication architecture (email verification OTP, password reset OTP, JWT tokens, BCrypt hashing, 60s resend cooldown, and attempt rate limiting) was preserved and verified end-to-end.

---

## 2. Files Modified

| File | Subsystem | Modifications |
| :--- | :--- | :--- |
| `backend/learning-path-backend/src/main/resources/application.properties` | Backend Config | Configured `spring.mail.*` properties wired to environment variables (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `OTP_DEV_LOGGING`). |
| `backend/learning-path-backend/src/main/java/com/learningpath/service/EmailService.java` | Email Service | Added `@Value("${app.otp.dev-logging:false}") private boolean devLogging;` to prevent exposing raw OTP codes in production logs while supporting local dev testing. |
| `backend/learning-path-backend/src/main/java/com/learningpath/entity/User.java` | Data Model | Adjusted `Boolean` wrapper types and null-safe accessors (`isEmailVerified()`, `isEmailNotifications()`, `isPushNotifications()`) so Hibernate updates table without constraint failures. |
| `backend/learning-path-backend/pom.xml` | Build Tooling | Configured Java 21 compatibility (`<java.version>21</java.version>`). |
| `.env.example` | Root Config | Updated template with documented SMTP, OTP, JWT, DB, and AI variables (zero secrets). |
| `backend/learning-path-backend/.env` | Backend Config | Updated environment file with SMTP/OTP variables preserving all existing DB, JWT, Gemini, and ML keys. |
| `.env` | Root Config | Synchronized root environment file. |
| `backend/learning-path-backend/src/test/java/com/learningpath/integration/OtpEmailAuthenticationIntegrationTest.java` | Test Suite | Built comprehensive 7-test suite for complete OTP lifecycle, cooldowns, purpose isolation, and reset flows. |

---

## 3. Environment Variable Specification

### 3.1 Mandatory Variables (for Live Email Delivery & Core Platform)

| Variable | Default / Format | Description |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://<host>:5432/postgres` | PostgreSQL / Supabase connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `<secret>` | Database password |
| `JWT_SECRET` | 256-bit Base64 String | Secret key for signing and validating JWT tokens |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server host (Gmail, SendGrid, Mailgun, etc.) |
| `MAIL_PORT` | `587` | SMTP port (587 for TLS / STARTTLS) |
| `MAIL_USERNAME` | `your-email@gmail.com` | SMTP authentication username / Gmail address |
| `MAIL_PASSWORD` | 16-character App Password | Google App Password (NOT your account password) |
| `MAIL_FROM` | `noreply@learnai.com` | From address for outgoing transactional emails |

### 3.2 Optional & Tuning Variables

| Variable | Default Value | Purpose |
| :--- | :---: | :--- |
| `OTP_EXPIRY_MINUTES` | `10` | Expiration window for verification and reset OTPs |
| `OTP_RESEND_COOLDOWN_SECONDS` | `60` | Minimum wait time before a new OTP can be requested |
| `OTP_MAX_ATTEMPTS` | `5` | Maximum incorrect verification attempts before OTP lockout |
| `OTP_DEV_LOGGING` | `false` | When `true`, prints OTP code to console for local testing |
| `GEMINI_API_KEY` | `<api-key>` | Google Gemini AI key for AI mentor & reasoning features |
| `GEMINI_MODEL` | `gemini-2.5-flash` | Gemini model version identifier |
| `ML_SERVICE_URL` | `http://localhost:8000` | Microservice URL for Python ML recommendation engine |
| `SERVER_PORT` | `8080` | Spring Boot backend HTTP server port |

---

## 4. Mail Provider & OTP Delivery Mechanism

### 4.1 Mail Provider
- **Technology:** Spring Boot JavaMail (`org.springframework.mail.javamail.JavaMailSender`) over **SMTP with STARTTLS**.
- **Supported Providers:** Gmail SMTP (`smtp.gmail.com:587`), SendGrid, Mailgun, Amazon SES, or custom SMTP servers.

### 4.2 How OTP Delivery Works
1. **Trigger:** Learner registers via `POST /api/auth/signup` or requests a password reset via `POST /api/auth/forgot-password`.
2. **Generation:** `OtpService` generates a 6-digit cryptographically secure number via `java.security.SecureRandom`.
3. **Hashing & Storage:** The raw OTP is hashed using **BCrypt** and persisted into the `otp_verifications` table with expiration timestamp (`Instant.now() + 10 mins`), purpose (`EMAIL_VERIFICATION` or `PASSWORD_RESET`), attempt count (`0`), and `used = false`.
4. **HTML Email Dispatch:** `EmailService` constructs a responsive HTML email containing LearnAI branding, the formatted 6-digit code box, expiration notice, and security warnings, sending it via `JavaMailSender.send(mimeMessage)`.
5. **Fallback:** If `JavaMailSender` is not configured (or in local dev mode), `EmailService` logs delivery progress safely without crashing or exposing codes in production logs.

---

## 5. Gmail App Password Setup Instructions

To enable live email delivery via Gmail SMTP:

```
Step 1: Go to Google Account Management (https://myaccount.google.com/).
Step 2: Navigate to "Security" -> "2-Step Verification" (ensure 2FA is turned ON).
Step 3: Scroll down to "App passwords" (or search "App passwords" in the search bar).
Step 4: Enter an App Name (e.g. "LearnAI Backend") and click "Create".
Step 5: Copy the generated 16-character code (e.g. abcd efgh ijkl mnop).
Step 6: In your .env file, configure:
        MAIL_HOST=smtp.gmail.com
        MAIL_PORT=587
        MAIL_USERNAME=your-actual-email@gmail.com
        MAIL_PASSWORD=your16characterapppassword
        MAIL_FROM=your-actual-email@gmail.com
```

> [!IMPORTANT]
> Never use your primary Google account login password. Always use a dedicated 16-character **Google App Password**.

---

## 6. Git Security & Secret Protection

- Verified that `.gitignore` at the repository root contains:
  ```gitignore
  # Environment
  .env
  .env.*
  !.env.example
  ```
- **Git Status Audit:** Confirmed that `.env` is **NOT tracked** by Git.
- **Example Template:** `.env.example` is tracked and contains only placeholder values with zero secrets.

---

## 7. OTP Security & Rate Limiting Controls

```
┌───────────────────────────────────────┬────────────────────────────────────────────────────────┐
│ Security Control                      │ Implementation Verification                            │
├───────────────────────────────────────┼────────────────────────────────────────────────────────┤
│ **Digit Length**                      │ Exactly 6 digits (Range: 100000 – 999999)               │
│ **Randomness Source**                 │ java.security.SecureRandom                             │
│ **Storage Security**                  │ BCrypt password hashing (never stored in plaintext)    │
│ **Expiration Window**                 │ 10 minutes (strictly validated before matching)        │
│ **Resend Cooldown**                   │ 60 seconds rate limit (rejects rapid requests)         │
│ **Superseded Invalidation**           │ Requesting a new OTP marks all previous OTPs as used   │
│ **Attempt Limit & Lockout**           │ Max 5 incorrect attempts before permanent invalidation │
│ **Purpose Isolation**                 │ EMAIL_VERIFICATION cannot be used for PASSWORD_RESET   │
│ **Single-Use Invariant**              │ OTP is marked used=true immediately upon verification   │
└───────────────────────────────────────┴────────────────────────────────────────────────────────┘
```

---

## 8. Test Execution & Build Verification

### 8.1 Phase 9 OTP Integration Test Suite
```powershell
mvn test -Dtest=OtpEmailAuthenticationIntegrationTest -f backend/learning-path-backend/pom.xml
```
**Outcome:** **Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 (BUILD SUCCESS)**  
- *Validation 1:* Signup generates unverified user, creates hashed OTP, and blocks login.
- *Validation 2:* Submitting correct OTP sets `emailVerified=true` and issues JWT.
- *Validation 3:* 60-second cooldown rejected rapid resend; resend after cooldown invalidated previous OTP.
- *Validation 4:* 5th incorrect attempt triggered permanent lockout; expired OTP rejected.
- *Validation 5:* Strict purpose separation between email verification and password reset.
- *Validation 6:* Forgot password dispatches reset OTP $\to$ verify returns single-use token $\to$ password updates.
- *Validation 7:* Production logging remains silent regarding raw OTP codes.

### 8.2 Frontend Production Build
```powershell
npm run build --prefix frontend-v2
```
**Outcome:** `tsc && vite build` bundled 2,188 modules with **0 TypeScript errors (Exit 0)** in 21.96s.

---

## 9. Next Steps for User Configuration

To activate live email delivery in your local `.env`:
1. Open `.env` in the project root or in `backend/learning-path-backend/.env`.
2. Fill in:
   - `MAIL_USERNAME=<your_gmail_address@gmail.com>`
   - `MAIL_PASSWORD=<your_16_character_app_password>`
   - `MAIL_FROM=<your_gmail_address@gmail.com>`
3. If you want OTP codes printed to console during local development, set `OTP_DEV_LOGGING=true`.
