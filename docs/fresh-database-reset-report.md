# Fresh Database Reset & Verification Report

**Project:** LearnAI — AI-Powered Personalized Learning Path Recommender  
**Date:** August 21, 2026  
**Operation:** Local Development Database Reset for Fresh Learner Testing  
**Status:** **COMPLETED & VERIFIED**

---

## 1. Executive Summary

The local development PostgreSQL database was safely cleared of all previous learner, test, and transient application records. All reference catalogs (courses, skills, careers, aliases, course mappings) and ML models were 100% preserved. A full JSON backup was taken prior to truncation. The platform is now in a pristine state ready for a first-time learner signup.

---

## 2. Table Inspection & Classification

### 2.1 User-Data Tables (CLEARED $\to$ 0 Rows)
1. `adaptive_assessment_responses`
2. `adaptive_assessment_sessions`
3. `ai_conversations`
4. `ai_messages`
5. `assessment_results`
6. `learner_knowledge_states`
7. `learning_activities`
8. `learning_path_versions`
9. `learning_paths` (and cascading `learning_path_items`)
10. `notifications`
11. `otp_verifications`
12. `support_tickets`
13. `user_progress`
14. `user_projects`
15. `user_skills`
16. `users`

### 2.2 Reference & Catalog Tables (PRESERVED)
1. `courses` (265 rows)
2. `course_skills` (282 rows)
3. `skills` (77 rows)
4. `careers` (5 rows)
5. `career_skills` (41 rows)
6. `skill_aliases` (61 rows)
7. `assessments` & `assessment_questions`
8. `projects`, `recommendations`, `recommendation_interactions`, `skill_prerequisites`, `user_interactions`

---

## 3. Pre-Reset Backup Status

- **Backup Type:** JSON Data Export of all user tables.
- **Location:** `scratch/backup_user_data_before_reset.json`
- **Data Exported:** 34 user records, 18 learning paths, 10 user progress entries, 6 OTP verification records.
- **Security Check:** Zero secrets or passwords exported to public directories.

---

## 4. Post-Reset Row Counts (Live Database Query)

| Table Name | Category | Pre-Reset Count | Post-Reset Count | Status |
| :--- | :---: | :---: | :---: | :---: |
| `users` | USER | 34 | **0** | **CLEARED** |
| `otp_verifications` | USER | 6 | **0** | **CLEARED** |
| `learning_paths` | USER | 18 | **0** | **CLEARED** |
| `learning_path_items` | USER (Child) | 72 | **0** | **CLEARED** |
| `user_progress` | USER | 10 | **0** | **CLEARED** |
| `user_skills` | USER | 0 | **0** | **CLEARED** |
| `learner_knowledge_states` | USER | 0 | **0** | **CLEARED** |
| `learning_activities` | USER | 0 | **0** | **CLEARED** |
| `adaptive_assessment_sessions` | USER | 0 | **0** | **CLEARED** |
| `adaptive_assessment_responses`| USER | 0 | **0** | **CLEARED** |
| `assessment_results` | USER | 0 | **0** | **CLEARED** |
| `ai_conversations` | USER | 0 | **0** | **CLEARED** |
| `ai_messages` | USER | 0 | **0** | **CLEARED** |
| `notifications` | USER | 0 | **0** | **CLEARED** |
| `support_tickets` | USER | 0 | **0** | **CLEARED** |
| `user_projects` | USER | 0 | **0** | **CLEARED** |
| `courses` | REFERENCE | 265 | **265** | **PRESERVED** |
| `course_skills` | REFERENCE | 282 | **282** | **PRESERVED** |
| `skills` | REFERENCE | 77 | **77** | **PRESERVED** |
| `careers` | REFERENCE | 5 | **5** | **PRESERVED** |
| `career_skills` | REFERENCE | 41 | **41** | **PRESERVED** |
| `skill_aliases` | REFERENCE | 61 | **61** | **PRESERVED** |

---

## 5. Verification of Fresh Pipeline

### 5.1 Signup & OTP Email Flow
- `POST /api/auth/signup` creates a fresh user with:
  - `emailVerified = false`
  - `onboardingCompleted = false`
  - BCrypt hashed password
  - 6-digit SecureRandom OTP stored as a BCrypt hash with 10-minute expiry
  - Branded HTML email dispatched via JavaMailSender over STARTTLS

### 5.2 Environment Configuration Audit
- `MAIL_HOST`: `smtp.gmail.com` (**CONFIGURED**)
- `MAIL_PORT`: `587` (**CONFIGURED**)
- `MAIL_USERNAME`: Configured in `.env`
- `MAIL_PASSWORD`: Configured in `.env` (Google App Password)
- `MAIL_FROM`: Configured in `.env`
- `OTP_EXPIRY_MINUTES`: `10` (**CONFIGURED**)
- `OTP_RESEND_COOLDOWN_SECONDS`: `60` (**CONFIGURED**)
- `OTP_MAX_ATTEMPTS`: `5` (**CONFIGURED**)
- `OTP_DEV_LOGGING`: `false` (**CONFIGURED**)

### 5.3 Onboarding Route Gating
- Newly registered user has `emailVerified=false` $\to$ Gated to `/verify-email`.
- Upon successful OTP submission $\to$ `emailVerified=true`, `onboardingCompleted=false` $\to$ Automatically redirected to `/onboarding`.
- Direct URL entry to `/dashboard`, `/learning-path`, `/assessments`, etc. is blocked by `ProtectedRoute.tsx` and redirected to `/onboarding`.
- Upon completing Onboarding Step 7 (`POST /api/onboarding/complete`) $\to$ `onboardingCompleted=true` $\to$ Transition to `/building-path` and `/dashboard`.

### 5.4 Machine Learning Microservice
- Model File: `ml-service/models/recommendation_model.joblib` (**PRESERVED & VERIFIED**)
- Active Version: `1.0` (10 feature columns)
- Microservice: Running at `http://localhost:8000` with `/health` reporting `UP`.

### 5.5 Gemini AI Mentor
- Server-side key configured in `.env`.
- Conversation table `ai_conversations` and `ai_messages` are at **0 rows**, providing a clean, fresh conversational state for new users.

---

## 6. Build & Test Verification Results

| Layer | Test Command | Result | Details |
| :--- | :--- | :---: | :--- |
| **Backend** | `mvn test -f backend/learning-path-backend/pom.xml` | **PASS** | 335 tests passed, 0 failures, 0 errors, 3 skipped |
| **Frontend** | `npm run build --prefix frontend-v2` | **PASS** | 2,189 modules bundled, 0 TypeScript errors |
| **ML Service**| `pytest -v` in `ml-service/` | **PASS** | 31 passed, 0 failures in 4.85s |

---

## 7. Instructions to Start Fresh Test

Run these 3 commands in separate terminals:

```powershell
# Terminal 1 — ML Recommendation Service
cd ml-service
$env:PYTHONPATH="."
.\.venv\Scripts\python.exe -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

# Terminal 2 — Spring Boot Backend
cd backend/learning-path-backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "C:\Program Files\Java\jdk-21\bin;" + $env:PATH
mvn spring-boot:run

# Terminal 3 — React Frontend Web App
cd frontend-v2
npm run dev
```

Navigate to `http://localhost:5173/signup` and sign up with a new email to experience the complete first-time learner flow.
