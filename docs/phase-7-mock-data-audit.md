# Phase 7 — Mock Data Removal & Data Grounding Audit Report

**Generated Date:** August 21, 2026  
**System:** AI-Powered Personalized Learning Path Recommender  
**Audit Scope:** Elimination of Fabricated Defaults & Verification of Real Data Integrity  
**Status:** COMPLETED & VERIFIED

---

## 1. Audit Objective

Prior to Phase 7, several services contained static fallback values designed during early prototyping (e.g. hardcoded learning hours of 12.5h, placeholder assessment scores of 65%, and simulated course enrollments). This audit verifies that all such fabrications have been eliminated and replaced with real queries that compute accurate statistics or return zero/empty states for new learners.

---

## 2. Codebase Audit & Refactoring Summary

### 2.1 `LearnerContextService.java`
- **Previous State:**
  - Injected `12.5` as `totalLearningHours` when progress repository returned null.
  - Fabricated 2 placeholder assessments when no assessments had been taken.
  - Imputed a default 65% `careerReadinessScore` regardless of learner state.
- **Remediated State:**
  - Computes `totalLearningHours` dynamically from `LearnerActivityRepository` and `CourseProgressRepository` (defaults strictly to `0.0` for new users).
  - Queries `AdaptiveAssessmentSessionRepository` for true completed assessments; returns an empty list (`List.of()`) if none exist.
  - Calculates `careerReadinessScore` using actual mastered skill weights over career target skills (returns `0%` for unassessed learners).
- **Verification:** `AIMentorHallucinationAndGroundingTest.testHallucinationResistance_missingCourseScore` verifies that a new learner prompt reflects `0.0` hours and empty course lists.

### 2.2 `AIMentorService.java`
- **Previous State:**
  - Injected synthetic advice stating "You have completed 3 assessments this week" when context was sparse.
  - Replaced missing knowledge probability with random normal distribution values.
- **Remediated State:**
  - Passes raw, unembellished context to Gemini 2.5 Flash prompt.
  - Explicitly instructs Gemini: *"If the learner has not taken assessments in a topic, explicitly state that no data exists and recommend taking an assessment."*
- **Verification:** `AIMentorHallucinationAndGroundingTest.testHallucinationResistance_missingPythonAssessment` confirms the mentor acknowledges absent data rather than inventing assessment scores.

### 2.3 `LearningPathRecalculationService.java`
- **Previous State:**
  - Mocked course recommendations when ML service was unreachable.
- **Remediated State:**
  - Returns `Optional.empty()` and falls back to deterministic rule-based skill gap ranking based strictly on the learner's database record.
- **Verification:** `FailureResilienceTest.testMlServiceOffline_returnsOptionalEmpty` validates clean fallback without synthetic data injection.

---

## 3. Data Grounding Audit Matrix

| Component | Audited Variable | Previous Fallback | Hardened Real Value |
| :--- | :--- | :--- | :--- |
| **Learner Context** | `totalLearningHours` | `12.5` | `0.0` (Sum of real session durations) |
| **Learner Context** | `assessmentAccuracy` | `0.70` | `0.0` or calculated from response history |
| **Learner Context** | `activeStreakDays` | `3` | `0` or computed from consecutive daily activities |
| **AI Prompting** | `recentAssessments` | 2 static assessments | `List.of()` (Real sessions only) |
| **AI Prompting** | `careerReadiness` | `65%` | Real weighted skill coverage `[0-100%]` |
| **Weekly Plan** | `dailyHours` | Hardcoded `2h` | Read from `user.dailyLearningHours` (user preference) |

---

## 4. Conclusion

All fabricated fallbacks have been eliminated from backend services and AI prompts. New learners now start with clean, unpolluted profiles, and AI responses remain strictly grounded in verified database state.
