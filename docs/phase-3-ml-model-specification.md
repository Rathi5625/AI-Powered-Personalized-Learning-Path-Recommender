# Phase 3 — ML Model Specification & Real Data Mapping
**LearnAI Personalized Learning Path Recommender**

---

## 1. Existing Model Overview

| Property | Specification |
|---|---|
| **Model Algorithm** | `GradientBoostingClassifier` (scikit-learn) |
| **Model Format** | `joblib` binary (`ml-service/models/recommendation_model.joblib`) |
| **Model Version** | `1.0` (active in Model Registry) |
| **Target Variable** | Binary recommendation decision (`recommended` ∈ {0, 1}) with continuous probability score |
| **Decision Threshold** | `probability >= 0.50` |
| **Evaluation Score** | Probability × 100 (0.0 to 100.0) |

---

## 2. Model Performance Metrics

Trained on 8,000 synthetic + audit samples, evaluated on a 2,000-sample test split:

- **Accuracy**: 80.20%
- **Precision**: 77.36%
- **Recall**: 74.97%
- **F1 Score**: 76.14%
- **ROC-AUC**: 0.8862
- **Cross-Validation F1 Mean**: 76.46% (± 0.0119)

---

## 3. Input Features Specification (10 Signals)

The model takes a normalized feature vector of 10 floating-point signals in the range `[0.0, 1.0]`:

| # | Feature Name | Range | Description |
|---|---|---|---|
| 1 | `skill_gap_score` | `[0.0, 1.0]` | Weighted score of skills taught by the course that match the learner's missing/unlearned skills. |
| 2 | `career_priority_score` | `[0.0, 1.0]` | Priority weight of the course's target domain relative to the learner's target career role. |
| 3 | `skill_coverage` | `[0.0, 1.0]` | Ratio of missing learner skills covered by this course vs total required skills for the career. |
| 4 | `proficiency_gap` | `[0.0, 1.0]` | Gap between learner's current verified proficiency in topic and course prerequisite/depth. |
| 5 | `difficulty_match` | `[0.0, 1.0]` | Alignment between learner experience level (`BEGINNER`/`INTERMEDIATE`/`ADVANCED`) and course difficulty. |
| 6 | `course_rating` | `[0.0, 1.0]` | Normalized course community rating (`rating / 5.0`). |
| 7 | `preference_match` | `[0.0, 1.0]` | Alignment with learner's preferred format (`VIDEO`, `PROJECT`, `READING`) and pace. |
| 8 | `mandatory_skill_match`| `[0.0, 1.0]` | Indicates whether the course teaches critical bottleneck core requirements. |
| 9 | `course_duration_match`| `[0.0, 1.0]` | Alignment between weekly commitment hours and course length. |
| 10| `course_quality_score` | `[0.0, 1.0]` | Composite score of provider reputation, completion rate, and verified curriculum. |

---

## 4. Real Database to Feature Mapping

The Spring Boot `LearnerFeatureBuilderService` maps real PostgreSQL database entities to the model input vector:

```
[User Profile / UserSkill / AssessmentResult] + [Course]
                         │
                         ▼
        LearnerFeatureBuilderService.java
                         │
                         ▼
         MlPredictionRequest (10 Features)
                         │
                         ▼
             POST /predict (FastAPI)
                         │
                         ▼
          GradientBoostingClassifier
                         │
                         ▼
   recommendation_probability & recommendation_score
```

### Signal Derivations from Real Database:
1. **`skill_gap_score`**: Calculated by intersecting `Course.tags` and `Course.title` with required career skills where learner has `UserSkill.proficiencyLevel < INTERMEDIATE` or no skill record.
2. **`career_priority_score`**: If `Course.category` matches `User.targetCareer` domain (e.g. Software Engineering -> Web Dev / CS / DSA), score = 1.0, otherwise 0.5.
3. **`difficulty_match`**:
   - `BEGINNER` learner + `BEGINNER` course -> 1.0
   - `BEGINNER` learner + `INTERMEDIATE` course -> 0.7
   - `BEGINNER` learner + `ADVANCED` course -> 0.3
   - `INTERMEDIATE` learner + `INTERMEDIATE` course -> 1.0
   - `ADVANCED` learner + `ADVANCED` course -> 1.0
4. **`course_rating`**: `Course.rating / 5.0` (defaults to `4.8 / 5.0 = 0.96` if not set).
5. **`preference_match`**: `1.0` if `User.preferredContentType` matches course format, `0.7` otherwise.
6. **`course_duration_match`**: Compares `Course.durationMinutes` with `User.weeklyCommitmentHours * 60`.

---

## 5. Inference Architecture & Endpoints

- **FastAPI ML Service**: `http://localhost:8000` (configurable via `ML_SERVICE_URL`)
- **Health Check**: `GET /health` -> `{"status": "UP", "model_loaded": true, "active_model": "1.0"}`
- **Model Info**: `GET /model-info` -> Returns features, accuracy, precision, recall, and ROC-AUC.
- **Inference**: `POST /predict` -> Evaluates single candidate course request.
