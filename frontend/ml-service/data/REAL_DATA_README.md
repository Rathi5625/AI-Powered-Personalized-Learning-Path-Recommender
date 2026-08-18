# Real Interaction Training Dataset Provenance & Guidelines

> **Notice**: This dataset contains real user interaction data and is intended for future model retraining.

## 1. Overview & Data Source
- **Data Source**: Exported interaction logs from the PostgreSQL table `recommendation_interactions`.
- **Target File**: `ml-service/data/real_interaction_training_data.csv`
- **Builder Script**: [`ml-service/training/build_real_dataset.py`](file:///c:/Users/parth/AI-Powered-Personalized-Learning-Path-Recommender/ml-service/training/build_real_dataset.py)

---

## 2. Interaction Consolidation & Label Mapping Logic

Learners navigate courses through multi-step journeys (e.g. `VIEWED` $\rightarrow$ `CLICKED` $\rightarrow$ `STARTED` $\rightarrow$ `COMPLETED`).
To avoid creating multiple contradictory training rows for a single user journey, interactions are grouped by `(user_id, course_id)` and mapped as follows:

| Interaction Signals | Target `recommendation_label` | Rationale |
| :--- | :---: | :--- |
| `STARTED`, `COMPLETED`, `LIKED` | `1` (Strong Positive) | Explicit active user engagement and completion. |
| `SKIPPED` | `0` (Negative) | Learner explicitly rejected the recommendation. |
| `CLICKED` | `1` (Weak Positive) | Learner opened course details. |
| `VIEWED` (Only) | *Excluded* | Insufficient evidence by itself (viewing without action does not imply preference). |

If both `STARTED`/`COMPLETED` and `SKIPPED` occur in a user journey, the positive active state takes precedence.

---

## 3. Minimum Data Sufficiency Threshold
- **Default Threshold**: Minimum **100** consolidated `(user_id, course_id)` pairs.
- **Label Diversity**: The dataset MUST contain both positive (`1`) and negative (`0`) examples.
- **Enforcement**: If the total consolidated count is lower than 100 or single-class only, `build_real_dataset.py` outputs:
  ```text
  Insufficient real interaction data for Model v2.
  ```
  And halts gracefully **without** writing synthetic/fake records.

---

## 4. Feature Representation (10 Model v1 Features)

The output dataset preserves the exact 10 numerical feature representation used by Recommendation Model v1:

1. `skill_gap_score` ($0.0 - 1.0$)
2. `career_priority_score` ($0.0 - 1.0$)
3. `skill_coverage` ($0.0 - 1.0$)
4. `proficiency_gap` ($0.0 - 1.0$)
5. `difficulty_match` ($0.0 - 1.0$)
6. `course_rating` ($0.0 - 5.0$)
7. `preference_match` ($0.0 - 1.0$)
8. `mandatory_skill_match` ($0.0 - 1.0$)
9. `course_duration_match` ($0.0 - 1.0$)
10. `course_quality_score` ($0.0 - 1.0$)

---

## 5. Differences: Real Interaction Data vs. Synthetic Data

| Metric | Synthetic Training Data (`training_data.csv`) | Real Interaction Data (`real_interaction_training_data.csv`) |
| :--- | :--- | :--- |
| **Generation** | Algorithmic domain heuristic simulation | Real learner actions on recommended courses |
| **Noise & Bias** | Zero real-world noise | Subject to user presentation bias (rank bias, UI visibility) |
| **Train/Test Splitting** | Random 80/20 stratified split | **User-level or Time-based splitting required** to prevent data leakage across user journeys |

---

## 6. Model v2 Retraining Notice
*Model v1 remains active in production.* This pipeline only extracts and prepares the real interaction dataset for evaluation when data volume requirements are satisfied.
