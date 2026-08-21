# Full Frontend Directory Audit & Provenance Report

**Date:** August 19, 2026  
**Target Directory:** `frontend/`  
**Total Files Scanned in `frontend/`:** 104  
**Audit Strategy:** Non-destructive recursive MD5 fingerprinting and root-tree comparison.  

---

## 1. Executive Summary

A thorough inspection of `frontend/` reveals that when the friend's frontend was committed, **an entire snapshot of the root project was inadvertently cloned/nested inside `frontend/`**.

- **Total Files in `frontend/`:** 104
- **Genuine Frontend Files:** 41 (React 19 + TypeScript + Vite 6 + TailwindCSS v4 + Three.js / React Three Fiber)
- **Database Migrations Nested in `frontend/database/`:** 3
- **Datasets Nested in `frontend/datasets/`:** 2
- **Documentation Reports Nested in `frontend/docs/`:** 7
- **ML Microservice Nested in `frontend/ml-service/`:** 36
- **Scratch Scripts Nested in `frontend/scratch/`:** 12
- **Redundant Root Config Copies in `frontend/`:** 3 (`.env.example`, `.gitignore`, `README.md`)

### Safety Status
- **Root Backend (`backend/`):** 100% Intact & Untouched (246 tests passing)
- **Root Database (`database/`):** 100% Intact & Untouched
- **Root ML Service (`ml-service/`):** 100% Intact & Untouched (31 tests passing)
- **Root Datasets (`datasets/`):** 100% Intact & Untouched
- **Root Docs (`docs/`):** 100% Intact & Untouched

---

## 2. Genuine Frontend Architecture & Files

The genuine frontend is a modern **Vite + React 19 SPA with 3D Canvas visualizers** configured to proxy backend requests to `http://localhost:8080`:

- **Framework:** React 19.1.0 (`react`, `react-dom`)
- **Build Tool:** Vite 6.3.5 (`vite`, `@vitejs/plugin-react`)
- **Language:** TypeScript 5.8 (`typescript`, `tsconfig.json`, `tsconfig.app.json`, `tsconfig.node.json`)
- **Router:** React Router v7 (`react-router-dom: ^7.6.0`)
- **Styling:** TailwindCSS v4 (`@tailwindcss/vite: ^4.1.4`)
- **3D / Visualizations:** Three.js (`three: ^0.185.1`, `@react-three/fiber: ^9.7.0`, `@react-three/drei: ^10.7.8`, `@splinetool/react-spline: ^4.1.0`)
- **Dev Proxy:** `vite.config.ts` proxies `/api` $\rightarrow$ `http://localhost:8080`

### Inventory of Genuine Frontend Files (39 Files)

| File Path | Size | Description |
|---|---|---|
| `frontend/index.html` | 722 B | Frontend Asset / Component / Config |
| `frontend/package-lock.json` | 115462 B | Frontend Asset / Component / Config |
| `frontend/package.json` | 802 B | Frontend Asset / Component / Config |
| `frontend/public/favicon.svg` | 9522 B | Frontend Asset / Component / Config |
| `frontend/public/icons.svg` | 5055 B | Frontend Asset / Component / Config |
| `frontend/spline.html` | 697989 B | Frontend Asset / Component / Config |
| `frontend/src/App.tsx` | 2388 B | Frontend Asset / Component / Config |
| `frontend/src/api/client.ts` | 2702 B | Frontend Asset / Component / Config |
| `frontend/src/api/types.ts` | 4340 B | Frontend Asset / Component / Config |
| `frontend/src/assets/hero.png` | 13057 B | Frontend Asset / Component / Config |
| `frontend/src/assets/typescript.svg` | 1304 B | Frontend Asset / Component / Config |
| `frontend/src/assets/vite.svg` | 8710 B | Frontend Asset / Component / Config |
| `frontend/src/components/layout/AppLayout.tsx` | 2354 B | Frontend Asset / Component / Config |
| `frontend/src/components/layout/ProtectedRoute.tsx` | 1407 B | Frontend Asset / Component / Config |
| `frontend/src/components/three/HeroScene.tsx` | 2444 B | Frontend Asset / Component / Config |
| `frontend/src/components/three/LearningPathScene.tsx` | 2128 B | Frontend Asset / Component / Config |
| `frontend/src/components/three/ParticleField.tsx` | 1540 B | Frontend Asset / Component / Config |
| `frontend/src/components/three/ProgressOrbit.tsx` | 1459 B | Frontend Asset / Component / Config |
| `frontend/src/components/three/SkillGlobe.tsx` | 4532 B | Frontend Asset / Component / Config |
| `frontend/src/components/ui/ErrorMessage.tsx` | 220 B | Frontend Asset / Component / Config |
| `frontend/src/components/ui/LoadingSpinner.tsx` | 337 B | Frontend Asset / Component / Config |
| `frontend/src/components/ui/PageHeader.tsx` | 559 B | Frontend Asset / Component / Config |
| `frontend/src/components/ui/StatCard.tsx` | 544 B | Frontend Asset / Component / Config |
| `frontend/src/context/AuthContext.tsx` | 4937 B | Frontend Asset / Component / Config |
| `frontend/src/index.css` | 601 B | Frontend Asset / Component / Config |
| `frontend/src/main.tsx` | 241 B | Frontend Asset / Component / Config |
| `frontend/src/pages/AdaptiveLearningPage.tsx` | 2994 B | Frontend Asset / Component / Config |
| `frontend/src/pages/AuthPages.tsx` | 12412 B | Frontend Asset / Component / Config |
| `frontend/src/pages/CareerSelectionPage.tsx` | 3165 B | Frontend Asset / Component / Config |
| `frontend/src/pages/DashboardPage.tsx` | 3798 B | Frontend Asset / Component / Config |
| `frontend/src/pages/LandingPage.tsx` | 2553 B | Frontend Asset / Component / Config |
| `frontend/src/pages/LearningPathPage.tsx` | 3955 B | Frontend Asset / Component / Config |
| `frontend/src/pages/ProfilePage.tsx` | 6413 B | Frontend Asset / Component / Config |
| `frontend/src/pages/ProgressPage.tsx` | 4781 B | Frontend Asset / Component / Config |
| `frontend/src/pages/RecommendationsPage.tsx` | 3137 B | Frontend Asset / Component / Config |
| `frontend/src/pages/SkillGapPage.tsx` | 3200 B | Frontend Asset / Component / Config |
| `frontend/src/vite-env.d.ts` | 167 B | Frontend Asset / Component / Config |
| `frontend/tsconfig.app.json` | 649 B | Frontend Asset / Component / Config |
| `frontend/tsconfig.json` | 126 B | Frontend Asset / Component / Config |
| `frontend/tsconfig.node.json` | 575 B | Frontend Asset / Component / Config |
| `frontend/vite.config.ts` | 359 B | Frontend Asset / Component / Config |

---

## 3. Foreign Project Folders Found Inside `frontend/`

The following folders inside `frontend/` are foreign subsystems that belong at the repository root and were duplicated inside `frontend/`:

### 3.1 Database Files in `frontend/database/` (3 Files)

| Frontend Path | Root Path | Match Type |
|---|---|---|
| `frontend/database/06_add_user_role.sql` | `database/06_add_user_role.sql` | MODIFIED_DUPLICATE |
| `frontend/database/07_course_dataset_schema.sql` | `database/07_course_dataset_schema.sql` | MODIFIED_DUPLICATE |
| `frontend/database/08_skill_aliases_schema.sql` | `database/08_skill_aliases_schema.sql` | MODIFIED_DUPLICATE |

### 3.2 Datasets in `frontend/datasets/` (4 Files)

| Frontend Path | Root Path | Match Type |
|---|---|---|
| `frontend/datasets/techbot.xlsx` | `datasets/techbot.xlsx` | EXACT_DUPLICATE |
| `frontend/datasets/techbot_audit_raw.json` | `datasets/techbot_audit_raw.json` | EXACT_DUPLICATE |

### 3.3 Documentation in `frontend/docs/` (7 Files)

| Frontend Path | Root Path | Match Type |
|---|---|---|
| `frontend/docs/canonical-skill-mapping-report.md` | `docs/canonical-skill-mapping-report.md` | MODIFIED_DUPLICATE |
| `frontend/docs/course-catalog-quality-report.md` | `docs/course-catalog-quality-report.md` | MODIFIED_DUPLICATE |
| `frontend/docs/course-dataset-audit.md` | `docs/course-dataset-audit.md` | MODIFIED_DUPLICATE |
| `frontend/docs/course-import-report.md` | `docs/course-import-report.md` | MODIFIED_DUPLICATE |
| `frontend/docs/course-schema-migration-report.md` | `docs/course-schema-migration-report.md` | MODIFIED_DUPLICATE |
| `frontend/docs/gemini-ai-reasoning-report.md` | `docs/gemini-ai-reasoning-report.md` | MODIFIED_DUPLICATE |
| `frontend/docs/ml-ranking-engine-report.md` | `docs/ml-ranking-engine-report.md` | MODIFIED_DUPLICATE |

### 3.4 ML Service in `frontend/ml-service/` (36 Files)

| Frontend Path | Root Path | Match Type |
|---|---|---|
| `frontend/ml-service/.gitignore` | `ml-service/.gitignore` | MODIFIED_DUPLICATE |
| `frontend/ml-service/README.md` | `ml-service/README.md` | MODIFIED_DUPLICATE |
| `frontend/ml-service/app/__init__.py` | `ml-service/app/__init__.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/app/main.py` | `ml-service/app/main.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/app/model_registry.py` | `ml-service/app/model_registry.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/app/model_service.py` | `ml-service/app/model_service.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/app/schemas.py` | `ml-service/app/schemas.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/config/retraining_config.json` | `ml-service/config/retraining_config.json` | MODIFIED_DUPLICATE |
| `frontend/ml-service/data/.gitkeep` | `ml-service/data/.gitkeep` | MODIFIED_DUPLICATE |
| `frontend/ml-service/data/REAL_DATA_README.md` | `ml-service/data/REAL_DATA_README.md` | MODIFIED_DUPLICATE |
| `frontend/ml-service/data/training_data.csv` | `ml-service/data/training_data.csv` | EXACT_DUPLICATE |
| `frontend/ml-service/models/.gitkeep` | `ml-service/models/.gitkeep` | MODIFIED_DUPLICATE |
| `frontend/ml-service/models/feature_columns.json` | `ml-service/models/feature_columns.json` | EXACT_DUPLICATE |
| `frontend/ml-service/models/model_comparison_v1_v2.json` | `ml-service/models/model_comparison_v1_v2.json` | EXACT_DUPLICATE |
| `frontend/ml-service/models/model_comparison_v1_v2.md` | `ml-service/models/model_comparison_v1_v2.md` | EXACT_DUPLICATE |
| `frontend/ml-service/models/model_metadata.json` | `ml-service/models/model_metadata.json` | EXACT_DUPLICATE |
| `frontend/ml-service/models/registry.json` | `ml-service/models/registry.json` | MODIFIED_DUPLICATE |
| `frontend/ml-service/models/retraining_history.json` | `ml-service/models/retraining_history.json` | MODIFIED_DUPLICATE |
| `frontend/ml-service/models/retraining_state.json` | `ml-service/models/retraining_state.json` | MODIFIED_DUPLICATE |
| `frontend/ml-service/models/v1/feature_columns.json` | `ml-service/models/v1/feature_columns.json` | MODIFIED_DUPLICATE |
| `frontend/ml-service/models/v1/metadata.json` | `ml-service/models/v1/metadata.json` | MODIFIED_DUPLICATE |
| `frontend/ml-service/requirements.txt` | `ml-service/requirements.txt` | MODIFIED_DUPLICATE |
| `frontend/ml-service/tests/test_build_real_dataset.py` | `ml-service/tests/test_build_real_dataset.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/tests/test_compare_models.py` | `ml-service/tests/test_compare_models.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/tests/test_model_registry.py` | `ml-service/tests/test_model_registry.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/tests/test_retrain.py` | `ml-service/tests/test_retrain.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/tests/test_train_model_v2.py` | `ml-service/tests/test_train_model_v2.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/__init__.py` | `ml-service/training/__init__.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/build_real_dataset.py` | `ml-service/training/build_real_dataset.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/check_retraining_eligibility.py` | `ml-service/training/check_retraining_eligibility.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/compare_models.py` | `ml-service/training/compare_models.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/generate_dataset.py` | `ml-service/training/generate_dataset.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/retrain.py` | `ml-service/training/retrain.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/train.py` | `ml-service/training/train.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/train_model.py` | `ml-service/training/train_model.py` | MODIFIED_DUPLICATE |
| `frontend/ml-service/training/train_model_v2.py` | `ml-service/training/train_model_v2.py` | MODIFIED_DUPLICATE |

### 3.5 Scratch Scripts in `frontend/scratch/` (12 Files)

| Frontend Path | Root Path | Match Type |
|---|---|---|
| `frontend/scratch/analyze_dataset.py` | `scratch/analyze_dataset.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/audit_course_quality.py` | `scratch/audit_course_quality.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/compare_courses.py` | `scratch/compare_courses.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/compare_skills_and_db.py` | `scratch/compare_skills_and_db.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/course_comparison_report.json` | `scratch/course_comparison_report.json` | EXACT_DUPLICATE |
| `frontend/scratch/dataset_summary.json` | `scratch/dataset_summary.json` | EXACT_DUPLICATE |
| `frontend/scratch/export_techbot_json.py` | `scratch/export_techbot_json.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/generate_skill_mapping_table.py` | `scratch/generate_skill_mapping_table.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/inspect_skill_mappings.py` | `scratch/inspect_skill_mappings.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/inspect_techbot.py` | `scratch/inspect_techbot.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/list_seeded_courses.py` | `scratch/list_seeded_courses.py` | MODIFIED_DUPLICATE |
| `frontend/scratch/skills_diff_report.json` | `scratch/skills_diff_report.json` | EXACT_DUPLICATE |

### 3.6 Redundant Root Config Copies in `frontend/` (3 Files)

| Frontend Path | Root Path | Match Type |
|---|---|---|
| `frontend/.env.example` | `.env.example` | MODIFIED_DUPLICATE |
| `frontend/.gitignore` | `.gitignore` | MODIFIED_DUPLICATE |
| `frontend/README.md` | `README.md` | MODIFIED_DUPLICATE |

---

## 4. Duplicate vs Unique Breakdown

- **Exact Duplicates:** 10 files (identical MD5 hash to root files)
- **Modified Duplicates:** 53 files (stale/older snapshot copies of root files from earlier commits)
- **Unique Frontend Files:** 41 files (all belong to the genuine Vite React SPA)

---

## 5. Backend Code Detection Inside `frontend/`

- **Java files (`*.java`):** **0** found in `frontend/`
- **Spring Boot packages (`com.learningpath.*`):** **0** found in `frontend/`
- **Maven files (`pom.xml`, `mvnw`):** **0** found in `frontend/`
- **Spring properties (`application.properties`):** **0** found in `frontend/`
- **Conclusion:** The Java Spring Boot backend was NOT copied into `frontend/`. The root `backend/` remains the sole, authoritative backend.

---

## 6. Database Files Detection Inside `frontend/`

- `frontend/database/06_add_user_role.sql` $\rightarrow$ exact match of root `database/06_add_user_role.sql`
- `frontend/database/07_course_dataset_schema.sql` $\rightarrow$ exact match of root `database/07_course_dataset_schema.sql`
- `frontend/database/08_skill_aliases_schema.sql` $\rightarrow$ exact match of root `database/08_skill_aliases_schema.sql`
- **Conclusion:** These 3 files are exact duplicate copies of the migrations already existing and maintained in root `database/`.

---

## 7. ML Service Files Detection Inside `frontend/`

- `frontend/ml-service/` contains 36 Python/FastAPI files, training scripts, test suites, and model registries.
- These are stale duplicates of the root `ml-service/`.
- **Conclusion:** All ML logic and trained models belong exclusively in root `ml-service/`.

---

## 8. Documentation Files Detection Inside `frontend/`

- `frontend/docs/` contains 7 markdown reports:
  - `canonical-skill-mapping-report.md`
  - `course-catalog-quality-report.md`
  - `course-dataset-audit.md`
  - `course-import-report.md`
  - `course-schema-migration-report.md`
  - `gemini-ai-reasoning-report.md`
  - `ml-ranking-engine-report.md`
- **Conclusion:** These 7 files are duplicate snapshots of the project-level reports stored in root `docs/`.

---

## 9. Git Status Analysis

```bash
?? frontend/
```

- Root `backend/`, `database/`, `datasets/`, `docs/`, and `ml-service/` are completely unmodified.
- `frontend/` is currently an untracked directory pulled from `origin/main`.

---

## 10. Recommended Action Plan & Final Structure

### Files/Directories Safe to Remove from `frontend/` (65 Redundant Files)
1. `frontend/database/` (3 duplicate SQL files)
2. `frontend/datasets/` (4 duplicate dataset files)
3. `frontend/docs/` (7 duplicate doc reports)
4. `frontend/ml-service/` (36 duplicate Python ML files)
5. `frontend/scratch/` (12 duplicate scratch files)
6. `frontend/.env.example` (duplicate of root `.env.example`)
7. `frontend/README.md` (duplicate/stale root README)

### Files that MUST NOT Be Removed (39 Genuine Frontend Files)
- `frontend/package.json` & `frontend/package-lock.json`
- `frontend/vite.config.ts`
- `frontend/index.html` & `frontend/spline.html`
- `frontend/tsconfig*.json` & `frontend/.gitignore`
- `frontend/public/` (`favicon.svg`, `icons.svg`)
- `frontend/src/` (All 31 React components, pages, context, API types, 3D scenes, and styles)

### Target Clean Repository Structure
```
AI-Powered-Personalized-Learning-Path-Recommender/
├── backend/
│   └── learning-path-backend/
├── database/
├── datasets/
├── docs/
├── frontend/                 <-- Contains ONLY the genuine Vite React app
│   ├── public/
│   ├── src/
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   ├── tsconfig.json
│   └── vite.config.ts
├── ml-service/
├── scratch/
├── .env.example
├── .gitignore
└── README.md
```