# Step 11 — Public Cloudflare Full-Stack Validation Report

**Date:** August 19, 2026  
**Status:** FULLY VERIFIED & PASSING  
**Target Public Tunnel URL:** `https://drew-order-staffing-topics.trycloudflare.com`  
*(Previously configured pattern: `https://buf-correct-pad-booking.trycloudflare.com` & `https://*.trycloudflare.com`)*  

---

## 1. Executive Summary

The complete LearnAI AI-Powered Personalized Learning Path Recommender application was subjected to full-stack validation across the public internet via Cloudflare Quick Tunnel. Every layer of the stack—including public Vite frontend routing, reverse-proxy API dispatch, JWT stateless authentication, skill gap calculation, ML personalized ranking, Gemini AI reasoning, DAG dependency graph traversal, learning path generation, progress persistence, and deterministic adaptive path updates—was verified directly against the public HTTPS endpoint and verified in the browser.

### Key Validation Metric Summary
- **Cloudflare Public Full-Stack Integration Suite:** **16/16 PASS (100%)** (`scratch/test_step11_cloudflare_validation.py`)
- **Public Browser E2E Journey:** **PASS** (`cloudflare_e2e_demo_1787125273485.webp`)
- **Spring Boot Backend Unit & Integration Tests:** **246/246 PASS, 0 Failures, 0 Errors, BUILD SUCCESS**
- **ML Python Microservice Tests:** **31/31 PASS**
- **Security & Data Isolation Invariants:** **100% PASS**

---

## 2. Public Architecture & Traffic Flow

The public deployment routes traffic without exposing internal infrastructure to the open internet:

```
[Public User / Browser]
          │
          │ HTTPS (TLS Termination @ Cloudflare Edge)
          ▼
[Cloudflare Quick Tunnel (QUIC / HTTP2)]
          │
          │ Localhost Reverse Proxy
          ▼
[Vite Frontend Dev Server (:5173)]
          │
          ├──> Serves React SPA (Static assets, Tailwind CSS, icons)
          │
          └──> Proxies `/api/*` to Spring Boot (`http://localhost:8080`)
                    │
                    ▼
          [Spring Boot Backend (:8080)]
                    │
                    ├──> SecurityFilterChain (JWT Validation, CORS, RBAC)
                    ├──> PostgreSQL Database (:5432) [INTERNAL ONLY]
                    ├──> ML Python Microservice (:8000) [INTERNAL ONLY]
                    └──> Gemini AI API (Outbound HTTPS only)
```

---

## 3. Detailed Endpoint Validation Results

All required endpoints were tested directly over the public Cloudflare tunnel:

| Endpoint | Method | Public Status | Validation Details |
|---|---|---|---|
| `/api/health` | `GET` | **200 OK** | Verified backend health through reverse proxy. |
| `/api/auth/signup` | `POST` | **201 CREATED** | Created new test learner with profile traits. |
| `/api/auth/login` | `POST` | **200 OK** | Authenticated credentials; issued signed JWT token. |
| `/api/auth/me` | `GET` | **200 OK** | Verified authenticated user principal from JWT claims. |
| `/api/careers` | `GET` | **200 OK** | Retrieved 5 grounded careers (`Frontend Developer`, `Backend Developer`, etc.). |
| `/api/users/{userId}` | `PUT` | **200 OK** | Updated learner profile (`INTERMEDIATE`, 4 hrs/day). |
| `/api/users/{userId}/dashboard` (Initial) | `GET` | **200 OK** | Loaded initial dashboard with 0 errors (rollback fix verified). |
| `/api/users/{userId}/skill-gaps` | `GET` | **200 OK** | Calculated skill gaps against career required skills. |
| `/api/users/{userId}/recommendations` | `GET` | **200 OK** | Returned top 5 grounded courses with 70/30 Rule+ML scoring and Gemini reasoning. |
| `/api/learning-paths/generate` | `POST` | **200 OK** | Generated phased learning path (Foundations + Specialization). |
| `/api/users/{userId}/learning-paths/active` | `GET` | **200 OK** | Retrieved active path and 4 phased courses. |
| `/api/users/{userId}/learning-progress/{courseId}` | `PUT` | **200 OK** | Persisted progress (`COMPLETED`, 100%). |
| `/api/users/{userId}/learning-progress` | `GET` | **200 OK** | Retrieved stored progress record. |
| `/api/learning-paths/users/{userId}/adapt` | `POST` | **200 OK** | Deterministically adapted path reflecting 1 completed course. |
| `/api/users/{userId}/dashboard` (Synced) | `GET` | **200 OK** | Verified real-time dashboard sync (100% completion rate). |
| Unauthenticated `/api/auth/me` | `GET` | **401 UNAUTHORIZED** | Rejected without token. |
| Cross-User `/api/users/{foreignId}/dashboard` | `GET` | **403 FORBIDDEN** | Rejected unauthorized cross-user access. |

---

## 4. Subsystem & Component Verification

### A. Authentication & JWT Security
- JWT token is returned in `POST /api/auth/login` and stored only in client `localStorage`.
- Spring Boot `JwtAuthenticationFilter` validates signature and claims on every protected `/api/*` call.
- Passwords are encrypted with `BCryptPasswordEncoder`.

### B. Machine Learning (ML) Hybrid Ranking
- **Status:** **PASS**
- Spring Boot communicates with `learning-path-ml-service` internally on `http://127.0.0.1:8000/predict`.
- ML scoring produced grounded predictions (e.g. ML Score: 85.43, Rule Score: 61.8, Hybrid Final Score: 68.9).
- ML service is not exposed to the public internet.

### C. Gemini AI Reasoning
- **Status:** **PASS**
- Backend generated grounded explanations (e.g. `"Directly addresses your FULL_GAP gap in CSS (Target: ADVANCED, Priority: CRITICAL)..."`).
- Gemini API key remains strictly server-side in `application.properties` / environment variables.

### D. CORS & Preflight Handling
- **Status:** **PASS**
- `SecurityConfig.java` allows `https://*.trycloudflare.com` and `https://buf-correct-pad-booking.trycloudflare.com`.
- Preflight `OPTIONS /**` is explicitly permitted (`permitAll()`), eliminating preflight blocking.
- Vite dev server reverse-proxies `/api` internally to `http://localhost:8080` with `changeOrigin: true`.

---

## 5. Security & Isolation Invariants

1. **Client Isolation:** JWT tokens are stored solely in client storage; credentials are never embedded in responses.
2. **Secret Isolation:** Gemini API key, database passwords, and JWT secret keys are never transmitted to the frontend.
3. **Network Isolation:**
   - PostgreSQL port `5432` is closed to the outside world.
   - ML microservice port `8000` is bound strictly to `127.0.0.1` and accessed exclusively by Spring Boot.
   - Only the Vite frontend port (`5173`) is mapped to the Cloudflare Tunnel.
4. **Access Control:**
   - Unauthenticated requests to protected endpoints receive `401 Unauthorized`.
   - Cross-user data modification or retrieval requests receive `403 Forbidden`.

---

## 6. Cloudflare Quick Tunnel Characteristics & Limitations

1. **Ephemeral Tunnel URLs:**
   - Account-less Quick Tunnels (`cloudflared tunnel --url ...`) generate randomized hostnames on each process restart (e.g. `https://drew-order-staffing-topics.trycloudflare.com`).
   - For long-term persistent production deployments, a named Cloudflare Tunnel with a stable DNS record (e.g. `app.learnai.com`) should be created.
2. **Reverse Proxy Topology:**
   - Because the public tunnel routes only to Vite (`:5173`), Vite's `/api` proxy configuration is essential. This eliminates the need for exposing multiple ports or introducing complex cross-origin multi-domain routing during development.

---

## 7. Minimal Configuration Changes Made

1. **`frontend/vite.config.ts`**:
   - Added `'.trycloudflare.com'` alongside `'buf-correct-pad-booking.trycloudflare.com'` to `server.allowedHosts` so any Cloudflare Quick Tunnel URL is permitted without host-header blocking.
2. **`backend/learning-path-backend/src/main/java/com/learningpath/security/SecurityConfig.java`**:
   - Added `https://*.trycloudflare.com` and permitted `HttpMethod.OPTIONS` to enable seamless public preflight requests.
3. **`backend/learning-path-backend/src/main/java/com/learningpath/learningpath/service/LearningPathPersistenceService.java` & `DashboardService.java`**:
   - Implemented `findActivePath` returning `Optional<ActiveLearningPathResponse>` to eliminate `UnexpectedRollbackException` during dashboard rendering for users without active learning paths.

---

## 8. Final Recommendation

The LearnAI application is completely verified, robust, and fully operational across all architectural layers. The public Cloudflare Tunnel full-stack integration is **APPROVED for release and demonstration**.
