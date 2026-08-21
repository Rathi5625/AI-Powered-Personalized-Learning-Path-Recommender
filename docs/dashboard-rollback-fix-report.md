# Dashboard "Transaction Rolled Back" Root Cause Analysis & Fix Report

**Date:** August 19, 2026  
**Status:** RESOLVED & VERIFIED  
**Target Endpoint:** `GET /api/users/{userId}/dashboard`  

---

## 1. Executive Summary & Root Cause

### Failing Endpoint
`GET /api/users/{userId}/dashboard`

### Root Cause & Why the Transaction Became Rollback-Only
1. `DashboardService.getDashboard(userId)` was annotated with `@Transactional(readOnly = true)`.
2. Inside `getDashboard`, it attempted to retrieve the user's active learning path via:
   ```java
   ActiveLearningPathResponse activePath = null;
   try {
       activePath = learningPathPersistenceService.getActivePath(userId);
   } catch (ResourceNotFoundException e) {
       log.debug("[DashboardService] No active learning path for userId={}", userId);
   }
   ```
3. `LearningPathPersistenceService.getActivePath(userId)` was also a transactional method (`@Transactional(readOnly = true)`). When a learner has not yet generated an active learning path (e.g. newly signed up user or un-onboarded admin), `getActivePath` executed:
   ```java
   LearningPath activePath = learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)
           .orElseThrow(() -> new ResourceNotFoundException("No active learning path found for user id: " + userId));
   ```
4. In Spring's Transaction Management architecture, when an inner `@Transactional` method throws a `RuntimeException` (such as `ResourceNotFoundException`), Spring's transaction interceptor intercepts the exception and marks the ambient physical transaction as **`rollback-only`** (`setRollbackOnly()`).
5. Even though `DashboardService` caught the `ResourceNotFoundException` in a `try/catch` block to gracefully default `activePath = null`, when `DashboardService.getDashboard()` completed and Spring attempted to commit the transaction, the Transaction Manager detected `isRollbackOnly() == true` and threw:
   ```
   org.springframework.transaction.UnexpectedRollbackException: Transaction silently rolled back because it has been marked as rollback-only
   ```
   resulting in an HTTP 500 response on the frontend dashboard.

---

## 2. Affected Backend Files

1. [`backend/learning-path-backend/src/main/java/com/learningpath/learningpath/service/LearningPathPersistenceService.java`](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/main/java/com/learningpath/learningpath/service/LearningPathPersistenceService.java)
2. [`backend/learning-path-backend/src/main/java/com/learningpath/service/DashboardService.java`](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/main/java/com/learningpath/service/DashboardService.java)
3. [`backend/learning-path-backend/src/test/java/com/learningpath/service/DashboardServiceTest.java`](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/test/java/com/learningpath/service/DashboardServiceTest.java)

---

## 3. Exact Fix Implemented

### 1. `LearningPathPersistenceService.java`
Added a non-exceptional query method `findActivePath(UUID userId)` that returns `Optional<ActiveLearningPathResponse>`. Re-routed `getActivePath(UUID userId)` to delegate to `findActivePath`:

```java
    /**
     * Finds the learner's active learning path without throwing when not found.
     *
     * @param userId The learner's UUID.
     * @return Optional containing the active path response if one exists, empty otherwise.
     */
    @Transactional(readOnly = true)
    public Optional<ActiveLearningPathResponse> findActivePath(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)
                .map(this::buildActivePathResponse);
    }

    /**
     * Retrieves the learner's active learning path.
     *
     * @param userId The learner's UUID.
     * @return {@link ActiveLearningPathResponse} containing path details and phased courses.
     * @throws ResourceNotFoundException if user or active path does not exist.
     */
    @Transactional(readOnly = true)
    public ActiveLearningPathResponse getActivePath(UUID userId) {
        return findActivePath(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active learning path found for user id: " + userId));
    }
```

### 2. `DashboardService.java`
Updated `DashboardService` to query `findActivePath` directly instead of relying on `try/catch` exception control flow:

```java
        // 2. Active Learning Path (null if none active)
        ActiveLearningPathResponse activePath = learningPathPersistenceService
                .findActivePath(userId)
                .orElse(null);
```

---

## 4. Verification & Test Results

### Automated Backend Tests
- **Command:** `.\mvnw.cmd clean test`
- **Result:** **246/246 tests passing, 0 failures, 0 errors, BUILD SUCCESS**

### ML Pytest Suite
- **Command:** `pytest`
- **Result:** **31/31 tests passing**

### Direct API Reproduction & Fix Verification
- **Before Fix:**
  - `GET /api/users/{newUserId}/dashboard` returned `HTTP 500 Internal Server Error` with `Transaction silently rolled back because it has been marked as rollback-only`.
- **After Fix:**
  - `GET /api/users/{newUserId}/dashboard` returns `HTTP 200 OK` with full user profile, `activeLearningPath: null`, `progressSummary: 0%`, and grounded skill gap data.

### Automated Full-System E2E Suite (`scratch/test_step10_full_system.py`)
- **Result:** **18/18 PASS (100%)**

### Browser E2E Verification (`dashboard_rollback_fix_demo_1787122681329.webp`)
- Authenticated through `http://localhost:5173/login` as `admin@learnai.local`.
- Successfully navigated to `http://localhost:5173/dashboard`.
- Verified zero rollback-only error banners.
- Progress Orbit, Target Career ("Frontend Developer"), Skill Gap Match (100%), and 5 Top Recommendations rendered cleanly.
- Screenshot: `dashboard_admin_success_1787122745020.png`.
