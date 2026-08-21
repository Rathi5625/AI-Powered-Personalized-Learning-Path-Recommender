# Phase 4 — Advanced Learner Modeling, Bayesian Knowledge Tracing & Adaptive Assessment Report
**LearnAI Personalized Learning Path Recommender**  
**Status**: COMPLETE  

---

## 1. Executive Summary

Phase 4 successfully upgraded LearnAI from a static recommendation system into a full **Adaptive Learning Intelligence Platform**.

Rather than relying on mock or hardcoded frontends, LearnAI now dynamically tracks:
1. What the learner knows vs does not know at a granular concept level.
2. The exact mathematical probability of mastery using **Bayesian Knowledge Tracing (BKT)**.
3. Behavior profiling including learning velocity, consistency, accuracy, preferred session duration, and explicit `INSUFFICIENT_DATA` handling.
4. Adaptive difficulty selection (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`) that smooths difficulty jumps and responds to streaks.
5. Dynamic assessment question selection and question difficulty calibration from real student attempts.
6. A multi-stage daily learning plan pipeline combining real learner knowledge, behavior profiles, the existing trained `GradientBoostingClassifier` ML model, and server-side Gemini explanations.
7. AI Mentor structured action suggestions (`START_ASSESSMENT`, `VIEW_LEARNING_PLAN`, `REVISE_TOPIC`, `EXPLORE_COURSE`).

---

## 2. System Architecture

```
                       REAL LEARNER INTERACTIONS
            (Assessment Questions, Course Progress, Activities)
                                   │
                                   ▼
                   BayesianKnowledgeTracingService
               (P(L0)=0.20, P(T)=0.15, P(G)=0.20, P(S)=0.10)
                                   │
                                   ▼
                 LearnerKnowledgeState (PostgreSQL)
       (Concept P(L), Streaks, Response Time, Revision Needed)
                                   │
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
LearnerMasteryService   LearnerBehaviorService   AdaptiveDifficultyService
(Mastered, Developing,  (Velocity, Consistency,  (BEGINNER / INTERMEDIATE /
 Weak, Revision Need)    Accuracy, Session Len)           ADVANCED)
         │                         │                         │
         └─────────────────────────┼─────────────────────────┘
                                   │
                                   ▼
                 PersonalizedLearningPlanService
                                   │
       ┌───────────────────────────┴───────────────────────────┐
       ▼                                                       ▼
Candidate Course Pool (DB)                             Top Weak Concepts
       │                                                       │
       ▼                                                       ▼
LearnerFeatureBuilderService                        Today's Focus Topic
(10 Normalized Signals)                             + Practice Challenges
       │                                            + Revision Session
       ▼
GradientBoostingClassifier (FastAPI ML)
(80.2% Accuracy, 0.8862 ROC-AUC)
       │
       └───────────────────────────┬───────────────────────────┘
                                   ▼
                    LearnerContextService (Context)
                                   │
                                   ▼
                 Gemini LLM (Server-Side Reasoning)
               (Zero-Hallucination Grounded Prompt)
                                   │
                                   ▼
                AIMentorService & React Frontend-v2
          (Mastery, Plans, Real BKT Progress, Actions)
```

---

## 3. Bayesian Knowledge Tracing (BKT) Formulation

The `BayesianKnowledgeTracingService` implements standard 4-parameter BKT:
- $P(L_0) = 0.20$: Prior probability of initial knowledge
- $P(T) = 0.15$: Probability of learning transition between interactions
- $P(G) = 0.20$: Probability of guessing correctly without mastery
- $P(S) = 0.10$: Probability of slipping (incorrect response despite mastery)

### Mathematical Update Rules:
1. **Observation Update**:
   - **Correct Response** ($obs = 1$):
     $$P(L_{t|\text{obs}}) = \frac{P(L_t) \cdot (1 - P(S))}{P(L_t) \cdot (1 - P(S)) + (1 - P(L_t)) \cdot P(G)}$$
   - **Incorrect Response** ($obs = 0$):
     $$P(L_{t|\text{obs}}) = \frac{P(L_t) \cdot P(S)}{P(L_t) \cdot P(S) + (1 - P(L_t)) \cdot (1 - P(G))}$$

2. **Learning Transition**:
   $$P(L_{t+1}) = P(L_{t|\text{obs}}) + (1 - P(L_{t|\text{obs}})) \cdot P(T)$$

---

## 4. Mastery Level Categorization

Configurable via `BktConfig`:
- **`0.00 – 0.29`**: `NOT_STARTED` / `VERY_WEAK`
- **`0.30 – 0.49`**: `DEVELOPING`
- **`0.50 – 0.69`**: `BASIC`
- **`0.70 – 0.84`**: `PROFICIENT`
- **`0.85 – 1.00`**: `MASTERED`

---

## 5. Question Calibration

`AssessmentQuestion` dynamically records student response statistics:
- `totalAttempts`
- `correctAttempts`
- `averageResponseTimeSeconds`
- `calibratedDifficulty`: After $\ge 5$ attempts, calibrated based on observed success rate ($>75\%$ -> `BEGINNER`, $40\%-75\%$ -> `INTERMEDIATE`, $<40\%$ -> `ADVANCED`).

---

## 6. Endpoints Created in Phase 4

| Method | Route | Description |
|---|---|---|
| `GET` | `/api/learner/mastery` | Returns overall mastery percentage, mastered, developing, and weak concepts. |
| `GET` | `/api/learner/weak-skills` | Returns list of weak/developing concepts ($P(L) < 0.50$). |
| `GET` | `/api/learner/revision` | Returns concepts flagged for revision due to consecutive failures. |
| `GET` | `/api/learner/behavior` | Returns velocity, consistency, accuracy, and data quality status. |
| `GET` | `/api/learner/difficulty` | Returns recommended difficulty for a concept. |
| `GET` | `/api/learning-plan` | Returns today's generated adaptive multi-stage plan. |
| `POST` | `/api/learning-plan/generate` | Generates / refreshes the daily learning plan. |
| `GET` | `/api/assessments/{id}/adaptive-question` | Selects next dynamic question based on current mastery and difficulty. |
| `POST` | `/api/assessments/{id}/adaptive-answer` | Processes answer, updates BKT probability, and calibrates question. |

---

## 7. Verification Results

| Test Suite | Command | Result |
|---|---|---|
| **Phase 4 Adaptive Tests** | `mvn test -Dtest=BayesianKnowledgeTracingServiceTest,...` | **PASSED** (10/10 tests passed) |
| **Complete Backend Test Suite** | `mvn test -f backend/learning-path-backend/pom.xml` | **`BUILD SUCCESS`** (257 tests run, 0 failures, 0 errors, 3 skipped) |
| **Frontend Production Build** | `npm run build` in `frontend-v2/` | **`BUILD SUCCESS`** (`tsc && vite build`, 0 TypeScript errors) |
| **Existing ML Model Inference** | `.venv/Scripts/python.exe -c "..."` in `ml-service/` | **PASSED** (score: 98.43, model: 1.0) |
