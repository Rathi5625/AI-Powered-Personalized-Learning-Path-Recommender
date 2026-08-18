# Course Dataset Database Schema Preparation Report

> **Document:** `docs/course-schema-migration-report.md`  
> **Status:** Step 2 Complete  
> **Target Dataset:** `datasets/techbot.xlsx` (244 Courses across 61 Skills)

---

## 1. Summary of Changes

To prepare the backend database and JPA architecture for importing the 244 curated course records from `datasets/techbot.xlsx`, the `Course` entity, repository, enums, DTOs, and database migration scripts have been updated safely and idempotently.

---

## 2. Fields Added vs. Fields Unchanged

### A. Fields Added to `Course` Entity & Schema
| Field Name | Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| `courseCode` (`course_code`) | `VARCHAR(50)` | `YES` (Unique) | Stores the dataset primary key identifier (`C001` - `C244`). |
| `youtubeTitle` (`youtube_title`)| `VARCHAR(255)` | `YES` | Stores resource YouTube title if video content is attached. |
| `youtubeUrl` (`youtube_url`) | `VARCHAR(500)` | `YES` | Stores direct YouTube video or playlist URL. |
| `youtubeNotes` (`youtube_notes`)| `TEXT` | `YES` | Stores video notes and companion metadata. |

### B. Fields Unchanged (Preserved from Existing Architecture)
| Field Name | Type | Status | Role in Dataset |
| :--- | :--- | :--- | :--- |
| `id` | `UUID` (Primary Key) | Unchanged | Canonical system UUID |
| `title` | `VARCHAR(200)` | Unchanged | Maps to Excel `title` column |
| `description` | `TEXT` | Unchanged | Detailed course description |
| `provider` | `VARCHAR(100)` | Unchanged | Maps to Excel `platform` column |
| `url` | `VARCHAR(500)` | Unchanged | Maps to Excel `link` column |
| `durationHours` | `DOUBLE PRECISION` | Unchanged | Maps to Excel `duration_hours` (3, 6, 10, 15) |
| `durationMinutes` | `INTEGER` | Unchanged | Derived duration in minutes |
| `difficulty` | `CourseDifficulty` | Extended | Supports `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `ALL_LEVELS`, `EASY`, `MEDIUM`, `HIGH` |
| `courseType` | `CourseType` | Extended | Supports `VIDEO_COURSE`, `INTERACTIVE_COURSE`, `TEXT_TUTORIAL`, `BOOTCAMP`, `PROJECT_BASED`, `DOCUMENTATION` |
| `language` | `VARCHAR(50)` | Unchanged | Default `"English"` |
| `rating` | `DECIMAL(3,2)` | Unchanged | Normalized rating (default `4.80` / `null`) |
| `price` | `DECIMAL(10,2)` | Unchanged | Course cost (default `0.00`) |
| `isFree` | `BOOLEAN` | Unchanged | Flags zero-cost learning resources (default `true` for dataset) |

---

## 3. Database Migration

- **Migration File**: [07_course_dataset_schema.sql](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/database/07_course_dataset_schema.sql)
- **Convention**: Sequential PostgreSQL/Supabase idempotent execution block (`DO $$ BEGIN ... END $$;`).
- **Indices Added**:
  - `idx_courses_course_code`: Partial unique index on `courses(course_code) WHERE course_code IS NOT NULL`.

---

## 4. Entity & Enum Updates

1. **[Course.java](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/main/java/com/learningpath/entity/Course.java)**:
   - Added `courseCode`, `youtubeTitle`, `youtubeUrl`, `youtubeNotes` with proper JPA column definitions and index mappings.
2. **[CourseDifficulty.java](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/main/java/com/learningpath/entity/enums/CourseDifficulty.java)**:
   - Added `EASY`, `MEDIUM`, `HIGH` values matching the Excel dataset difficulty tiers.
   - Added `fromDatasetLevel(String level)` helper for clean ingestion parsing.
3. **[CourseType.java](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/main/java/com/learningpath/entity/enums/CourseType.java)**:
   - Added `DOCUMENTATION` type and `fromPlatform(String platform)` parser helper.
4. **[CourseRepository.java](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/main/java/com/learningpath/repository/CourseRepository.java)**:
   - Added `findByCourseCode(String courseCode)` and `existsByCourseCode(String courseCode)`.
5. **[RecommendationScoringEngine.java](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/main/java/com/learningpath/recommendation/engine/RecommendationScoringEngine.java)**:
   - Updated `calculateDifficultyMatch` and `calculateUserPreference` to cleanly support all 7 enum difficulty tiers and `DOCUMENTATION` content types.

---

## 5. API & Data Compatibility Status

- **Backward Compatibility**: **100% Compatible**.
- `CourseResponse` and `CourseRequest` DTOs contain overloaded constructors preserving legacy 14-parameter signatures.
- All existing 21 seeded courses in `CourseDataInitializer.java` remain fully functional with `courseCode = null`.
- Zero Breaking Changes to existing REST endpoints (`GET /api/courses`, `POST /api/courses`, `GET /api/courses/{id}`, `/api/recommendations/**`, `/api/learning-paths/**`).

---

## 6. Automated Test Results

- **New Integration Suite**: [CourseSchemaIntegrationTest.java](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/backend/learning-path-backend/src/test/java/com/learningpath/course/CourseSchemaIntegrationTest.java)
  - `1. Persist Course with dataset courseCode and YouTube metadata` $\rightarrow$ **Passed**
  - `2. Persist Course with null dataset fields for backward compatibility` $\rightarrow$ **Passed**
  - `3. CourseService create and update with new dataset DTO fields` $\rightarrow$ **Passed**
  - `4. Verify CourseDifficulty enum parsing from dataset levels` $\rightarrow$ **Passed**
  - `5. Verify CourseType enum parsing from provider platforms` $\rightarrow$ **Passed**
  - `6. Existing seeded courses remain accessible and intact` $\rightarrow$ **Passed**
- **Full Backend Maven Test Suite**:
  - Command: `.\mvnw.cmd clean test`
  - Total Tests Run: **213**
  - Failures: **0**
  - Errors: **0**
  - Skipped: **1**
  - Status: **`BUILD SUCCESS`**

---

## 7. Unresolved Issues / Blockers

- **None**: Database schema, entity layers, repositories, and DTOs are prepared and verified for the dataset ingestion step.
