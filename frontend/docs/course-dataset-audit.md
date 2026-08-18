# Comprehensive Course Dataset and Backend Architecture Audit

> **Document:** `docs/course-dataset-audit.md`  
> **Status:** Step 1 Complete (Audit Only — Zero Code or Schema Modifications Made)  
> **Target Application:** LearnAI — AI-Powered Personalized Learning Path Recommender

---

## 1. Dataset Summary

The external course dataset is located in the project repository at `datasets/techbot.xlsx`.

| Metric | Value | Description |
| :--- | :--- | :--- |
| **Filename** | `datasets/techbot.xlsx` | Excel workbook containing validated curriculum resources |
| **Total Worksheets** | `2` | `Courses` and `README` |
| **Total Course Rows** | `244` | Exactly 244 learning resource records ($61 \text{ skills} \times 4 \text{ difficulty levels}$) |
| **Total Skills** | `61` | 32 Frontend Track skills + 29 Backend Track skills |
| **Difficulty Levels** | `4` | `Beginner` (61), `Easy` (61), `Medium` (61), `High` (61) |
| **Duration Hours** | `4 distinct` | 3 hrs (Beginner), 6 hrs (Easy), 10 hrs (Medium), 15 hrs (High) |
| **Unique Platforms** | `95` | MDN, web.dev, freeCodeCamp, GitHub, Microsoft, Google, Redis, etc. |
| **Missing Values (Nulls)** | `0` | All columns in all 244 rows are fully populated |
| **Malformed URLs** | `0` | All 244 URLs are valid, parseable HTTP/HTTPS endpoints |
| **YouTube Links** | `0` | Zero video/YouTube links; all point to direct documentation/courseware |
| **Duplicate Course IDs** | `0` | All IDs (`C001` through `C244`) are unique primary keys |
| **Duplicate (Skill, Level)** | `0` | Each of the 61 skills has exactly 1 course per difficulty level |

---

## 2. Excel Worksheet Structure

### Worksheet 1: `Courses`
- **Total Rows**: 245 (1 header row + 244 data rows)
- **Columns (7)**:
  1. `course_id` (String): e.g. `C001`, `C002`, ..., `C244`
  2. `title` (String): Resource title (e.g. `MDN: Structuring content with HTML`, `web.dev: Learn CSS`)
  3. `skill_tag` (String): Associated skill tag (e.g. `HTML`, `CSS`, `React`, `REST APIs`)
  4. `level` (String): Difficulty tier (`Beginner`, `Easy`, `Medium`, `High`)
  5. `duration_hours` (Integer): Estimated duration (`3`, `6`, `10`, `15`)
  6. `platform` (String): Content provider / host (`MDN`, `web.dev`, `freeCodeCamp`, `GitHub`, `Redis`, `Microsoft`, etc.)
  7. `link` (String): Direct URL to the learning resource

#### Distribution of Difficulty Levels & Durations:
- `Beginner`: 61 courses | 3.0 hours duration
- `Easy`: 61 courses | 6.0 hours duration
- `Medium`: 61 courses | 10.0 hours duration
- `High`: 61 courses | 15.0 hours duration

#### Top Content Platforms:
1. **MDN (Mozilla Developer Network)**: 46 courses
2. **web.dev (Google Chrome DevRel)**: 14 courses
3. **freeCodeCamp**: 13 courses
4. **GitHub Documentation / Guides**: 11 courses
5. **Google / Google Developers**: 9 courses
6. **Microsoft Learn / Docs**: 9 courses
7. **Redis Documentation / University**: 5 courses
8. **RESTful API / Postman / University of Helsinki**: 4 courses each

#### Duplicate Titles and Links in Source Dataset:
- **Duplicate Titles**: 43 titles are reused across 120 rows (e.g. `freeCodeCamp: Responsive Web Design` is assigned across 5 related CSS/Design skills; `MDN: Introduction to HTML` is used for both Beginner and Easy tiers).
- **Duplicate Links**: 43 URLs are referenced across 125 rows.

### Worksheet 2: `README`
- **Total Rows**: 7 (1 header + 6 data rows)
- **Metadata Recorded**:
  - `Source of truth`: *"Exact finalized JSON supplied by user"*
  - `Skills`: *"61 actual skills (32 Frontend + 29 Backend)"*
  - `Rows`: *"244 (61 × 4)"*
  - `Levels per skill`: *"Beginner, Easy, Medium, High"*
  - `Search-result links`: *"None used; links point to specific learning resources/tutorial/course pages"*
  - `Duration`: *"Estimated learning time; not provider-certified"*

---

## 3. Existing Database Course Architecture

The current backend database Course model is defined in `com.learningpath.entity.Course`:

```java
public class Course extends BaseEntity {
    private String title;              // VARCHAR(200), NOT NULL
    private String description;        // TEXT
    private String provider;           // VARCHAR(100), NOT NULL
    private String url;                // VARCHAR(500)
    private Integer durationMinutes;   // INTEGER
    private Double durationHours;      // DOUBLE PRECISION
    private CourseType courseType;     // ENUM: VIDEO_COURSE, INTERACTIVE_COURSE, ARTICLE, BOOK, PROJECT_BASED, SPECIALIZATION
    private String language;           // VARCHAR(50), default "English"
    private CourseDifficulty difficulty; // ENUM: BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS
    private BigDecimal rating;         // DECIMAL(3,2)
    private BigDecimal price;          // DECIMAL(10,2)
    private boolean isFree;            // BOOLEAN, default false
}
```

### Course Join Mapping: `CourseSkill` (`com.learningpath.entity.CourseSkill`)
- Many-to-One with `Course` (`course_id`)
- Many-to-One with `Skill` (`skill_id`)
- `coverageLevel`: `CoverageLevel` (`BASIC`, `INTERMEDIATE`, `COMPREHENSIVE`, `SPECIALIZED`)
- `importance`: `SkillPriority` (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`)
- `targetProficiency`: `ProficiencyLevel` (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`)
- `isPrimarySkill`: `boolean`

### Current Seed State:
- `CourseDataInitializer.java` currently seeds **21 hardcoded courses** (e.g. `Java Programming Fundamentals`, `Object-Oriented Programming with Java`, `Spring Boot 3 Fundamentals`, `Docker & Containers for Java Developers`).
- Currently, **0 of the 244 Excel courses** are in the database.

---

## 4. Existing Skill Architecture

The current Skill model is defined in `com.learningpath.entity.Skill`:

```java
public class Skill extends BaseEntity {
    private String name;               // VARCHAR(100), NOT NULL, UNIQUE
    private String category;           // VARCHAR(50), NOT NULL
    private String description;        // TEXT
    private SkillDifficulty difficulty;// ENUM: BEGINNER, INTERMEDIATE, ADVANCED
}
```

### Current Catalogs in Codebase:
1. **`CareerDataInitializer.java`**: Seeds **25 skills** (`Java`, `OOP`, `SQL`, `Spring Boot`, `HTML`, `CSS`, `JavaScript`, `React`, `Python`, `Machine Learning`, etc.).
2. **`SkillDependencyService.java`**: Loads **65 skills** across 3 tracks from `classpath:data/skill_prerequisites.json` for topological DAG ordering.

---

## 5. Dataset vs. Database Differences

### Numerical Discrepancies
| Dimension | Database Seed | SkillDependency DAG | Excel Dataset (`techbot.xlsx`) |
| :--- | :--- | :--- | :--- |
| **Courses** | 21 courses | N/A | **244 courses** |
| **Skills** | 25 skills | 65 skills | **61 skills** |

### Skill Set Gaps
- **57 skills present in Excel are missing from the Seeded Database**:
  - Frontend: `Accessibility`, `Design Systems`, `Browser Web APIs`, `Web Components`, `Progressive Web Apps`, `Module Bundlers`, `CSS Frameworks`, `Generative AI for Frontend`, `Desktop Applications in JavaScript`, etc.
  - Backend: `Auth Strategies`, `Caching`, `Building for Scale`, `Message Brokers`, `Scaling Databases`, `Search Engines`, `Real Time Data`, `CI/CD Basics`, `CLI & Terminal Basics`, `Learn about Web Servers`, `Node.js Basics`, `Express.js (Web Framework)`, `Django or Flask (Web Framework)`, etc.
- **21 skills present in Seeded Database are missing from Excel**:
  - `Java`, `Spring Boot`, `Spring Security`, `JPA/Hibernate`, `OOP`, `Data Structures & Algorithms`, `Docker`, `Pandas`, `NumPy`, `Statistics`, `Deep Learning`, `TensorFlow/PyTorch`, `MLOps`, etc.
- **5 skills in Excel missing from `skill_prerequisites.json` DAG**:
  - `CSS Fundamentals`, `HTML Fundamentals`, `Internet Fundamentals`, `JavaScript Foundations`, `Python Basics`.

### Inconsistent Skill Naming / Near-Aliases in Excel:
The Excel dataset itself contains overlapping / near-alias skill tags that should be mapped carefully without silent loss of data:
1. `'AI Assisted Coding'` vs `'AI-Assisted Coding'` (both present in Excel)
2. `'CSS'` vs `'CSS Fundamentals'` (both present in Excel)
3. `'HTML'` vs `'HTML Fundamentals'` (both present in Excel)
4. `'Internet Basics'` vs `'Internet Fundamentals'` (both present in Excel)
5. `'JavaScript'` vs `'JavaScript Foundations'` (both present in Excel)
6. `'Version Control(Git & GitHub)'` vs `'Git'` / `'Git & GitHub'`
7. `'Databases (SQL)'` vs `'SQL'`
8. `'NoSQL Databases'` vs `'SQL Databases'`

---

## 6. Required Schema Changes

To cleanly support the 244 external courses without breaking existing entities or queries:

1. **`courses` table**:
   - Add `course_code` column: `VARCHAR(50) NULL UNIQUE` (stores `C001` - `C244`).
   - Add index on `course_code`: `CREATE INDEX idx_courses_code ON courses(course_code);`.
   - Retain `duration_hours` (`DOUBLE PRECISION`) and `url` (`VARCHAR(500)`).
2. **`CourseDifficulty` Enum Expansion**:
   - Existing: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `ALL_LEVELS`
   - Excel uses: `Beginner`, `Easy`, `Medium`, `High`
   - Option A (Direct Mapping): Map `Beginner` $\rightarrow$ `BEGINNER`, `Easy` $\rightarrow$ `BEGINNER`/`INTERMEDIATE`, `Medium` $\rightarrow$ `INTERMEDIATE`, `High` $\rightarrow$ `ADVANCED`.
   - Option B (Enum Addition): Add `EASY`, `MEDIUM`, `HIGH` (or `HARD`) to `CourseDifficulty` enum to represent the 4 levels directly.

---

## 7. Required Entity Changes

1. **`Course.java`**:
   ```java
   @Column(name = "course_code", length = 50, unique = true)
   private String courseCode;
   ```
2. **`CourseDifficulty.java`**:
   Update enum or implement parser/mapper to ingest `Beginner`, `Easy`, `Medium`, `High` cleanly.

---

## 8. Required Importer Changes

Create a clean, idempotent Excel/JSON course importer component (`TechbotCourseImporter`):
1. Reads `datasets/techbot.xlsx` (or a canonical JSON export).
2. Checks if each skill exists in `skills` table; creates missing skills with appropriate track category (`Frontend`, `Backend`).
3. For each of the 244 rows:
   - Finds or creates `Course` entity with `courseCode = course_id`, `title`, `provider = platform`, `url = link`, `durationHours = duration_hours`, `isFree = true`.
   - Upserts `CourseSkill` mapping associating the course with its corresponding `skill_tag`.
4. Idempotent execution (skips or updates existing records without creating duplicates).

---

## 9. Skill Mapping Requirements

To reconcile the 61 Excel skills with the 65 DAG skills and 25 Career skills:
1. Preserve canonical skill names while creating explicit alias mappings in `SkillDependencyService.java`:
   - `"AI-Assisted Coding"` $\leftrightarrow$ `"AI Assisted Coding"`
   - `"HTML Fundamentals"` $\rightarrow$ `"HTML"`
   - `"CSS Fundamentals"` $\rightarrow$ `"CSS"`
   - `"JavaScript Foundations"` $\rightarrow$ `"JavaScript"`
   - `"Internet Fundamentals"` $\rightarrow$ `"Internet Basics"`
   - `"Databases (SQL)"` $\leftrightarrow$ `"SQL Databases"` / `"SQL"`
   - `"Version Control(Git & GitHub)"` $\rightarrow$ `"Git & GitHub"`
2. Keep raw Excel skill tags intact during import so that course filtering by `skill_tag` matches 100% of candidate courses.

---

## 10. Recommendation Pipeline Audit

### Current Recommendation Flow:
$$\text{User} \longrightarrow \text{Target Career} \longrightarrow \text{Required Skills} \longrightarrow \text{Learner Skills} \longrightarrow \text{Skill Gaps} \longrightarrow \text{Candidate Courses} \longrightarrow \text{Scoring Engine} \longrightarrow \text{Ranked Recs}$$

1. **Skill Gap Analysis**:
   - `SkillGapService.java` compares `UserSkill` vs `CareerSkill`.
   - Categorizes gaps into `NO_GAP`, `PARTIAL_GAP`, `FULL_GAP`.
2. **Candidate Generation**:
   - `RecommendationService.java` queries `CourseSkillRepository.findBySkillIdIn(gapSkillIds)`.
3. **Rule-Based Scoring Engine** (`RecommendationScoringEngine.java`):
   - Computes weighted score across 6 factors:
     - **Skill Gap Match (35%)**: Measures critical/mandatory missing skills covered.
     - **Career Priority (20%)**: Higher weight for `CRITICAL` career skills.
     - **Skill Coverage (15%)**: Ratio of gaps covered by the single course.
     - **Difficulty Match (10%)**: Aligning user experience level with course difficulty.
     - **Course Quality (10%)**: Normalizing rating/provider reputation.
     - **User Preference (10%)**: Content type & free/paid preferences.
4. **Final Scoring Formula**:
   $$\text{FinalScore} = \begin{cases} 
   0.60 \times \text{RuleScore} + 0.40 \times \text{MLScore} & \text{if ML Service Available} \\
   \text{RuleScore} & \text{if ML Service Unavailable}
   \end{cases}$$

---

## 11. ML Integration Audit

- **Client**: `MlRecommendationClient.java`
- **Microservice Endpoint**: `POST http://localhost:8000/predict` (FastAPI / Scikit-Learn)
- **Feature Vector (10 features)**:
  1. `skill_gap_score`
  2. `career_priority_score`
  3. `skill_coverage`
  4. `proficiency_gap`
  5. `difficulty_match`
  6. `course_rating`
  7. `preference_match`
  8. `mandatory_skill_match`
  9. `course_duration_match`
  10. `course_quality_score`
- **Failure Resilience**: If the ML service is down, timed out, or returns an error, `MlRecommendationClient` logs a warning and returns `Optional.empty()`. The system automatically falls back to deterministic rule scoring without crashing.

---

## 12. Gemini / AI Integration Audit

- **Client**: `GeminiClient.java` calling Google Gemini API (`gemini-2.5-flash`).
- **Prompt Generator**: `LearningPathPromptBuilder.java` compiles the full learner context, skill gaps, topological prerequisite DAG, and grounded candidate course list.
- **Grounding & Hallucination Prevention**:
  - `LearningPathValidator.java` inspects every course ID returned by Gemini.
  - Verifies that every course ID exists in `candidateCourses`.
  - Verifies that course titles and providers match canonical database records.
  - Rejects ungrounded, hallucinated, or malformed course IDs.
- **Fail-Safe Mechanism**:
  - If Gemini fails, times out, or returns a response that fails grounding/topological validation after 1 retry, `PersonalizedLearningPathService.java` automatically triggers `generateRuleBasedFallback(context)`.
  - **Result**: The system is 100% resilient and never displays hallucinated or broken course data to the learner.

---

## 13. Learning Path Audit (`POST /api/learning-paths/generate`)

1. **Prerequisite DAG Ordering**:
   - `SkillDependencyService.java` performs Kahn's topological sort on target skill gaps to generate ordered prerequisite phases.
2. **Phase Partitioning**:
   - Courses are assigned to phases matching prerequisite tiers (e.g. Fundamentals $\rightarrow$ Intermediate Frameworks $\rightarrow$ Advanced Architecture).
3. **Database Persistence**:
   - `LearningPathPersistenceService.java` archives any previous `ACTIVE` path for the learner and persists the new path with `LearningPathItem` records referencing real database `Course` entities.

---

## 14. Risks & Mitigations

| Risk | Impact | Mitigation |
| :--- | :--- | :--- |
| **Skill Name Divergence** | Candidates not found if career skills do not match Excel skill tags | Implement bidirectional alias resolution in `SkillDependencyService` and database mapping |
| **Difficulty Level Mismatch** | `Beginner`, `Easy`, `Medium`, `High` in Excel vs `BEGINNER`, `INTERMEDIATE`, `ADVANCED` in DB | Implement explicit difficulty mapping enum parser |
| **Duplicate Titles Across Levels** | 43 titles shared across levels/skills | Identify courses by unique `course_code` (`C001` - `C244`) instead of title string |
| **AI Hallucination** | LLM inventing non-existent course URLs | Strict grounding validator rejects any course ID not in candidate set |

---

## 15. Recommended Implementation Order (Future Steps)

1. **Step 2 — Schema & Entity Upgrade**:
   - Add `course_code` to `Course` entity and database migration.
   - Update `CourseDifficulty` or level parser for `Beginner`, `Easy`, `Medium`, `High`.
2. **Step 3 — Dataset Ingestion Service**:
   - Build automated importer to seed all 61 skills and 244 courses from `datasets/techbot.xlsx`.
3. **Step 4 — Skill Graph & Alias Harmonization**:
   - Synchronize `data/skill_prerequisites.json` with the 61 imported skills and register aliases.
4. **Step 5 — Recommendation & Learning Path Integration**:
   - Verify candidate generation and scoring with the 244 courses.
5. **Step 6 — End-to-End Testing & Verification**:
   - Run full integration test suite ensuring zero regressions.
