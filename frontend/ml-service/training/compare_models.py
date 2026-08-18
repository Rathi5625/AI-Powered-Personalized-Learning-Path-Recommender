"""
Model V1 vs Model V2 Formal Evaluation & Comparison Pipeline (Step 9H-3).

This script compares production Model v1 against Candidate Model v2 using a common
real interaction evaluation dataset, calculates metrics, formats comparison tables,
applies promotion threshold rules, and outputs decision reports without overwriting Model v1.
"""

import json
import os
import sys
import datetime
from pathlib import Path
import pandas as pd
import numpy as np
import joblib

from sklearn.model_selection import StratifiedKFold, cross_val_score
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score, roc_auc_score, confusion_matrix
)

FEATURE_COLUMNS = [
    "skill_gap_score",
    "career_priority_score",
    "skill_coverage",
    "proficiency_gap",
    "difficulty_match",
    "course_rating",
    "preference_match",
    "mandatory_skill_match",
    "course_duration_match",
    "course_quality_score"
]

TARGET_COLUMN = "recommendation_label"
MIN_EVAL_SAMPLES = 100
MIN_F1_IMPROVEMENT = 0.01
MAX_ROCAUC_DEGRADATION = 0.02
RANDOM_STATE = 42


def evaluate_model_on_data(model, X: pd.DataFrame, y: pd.Series) -> dict:
    """
    Evaluates a trained model on specified features X and target y.
    """
    y_pred = model.predict(X)
    y_prob = model.predict_proba(X)[:, 1]

    accuracy = float(accuracy_score(y, y_pred))
    precision = float(precision_score(y, y_pred, zero_division=0))
    recall = float(recall_score(y, y_pred, zero_division=0))
    f1 = float(f1_score(y, y_pred, zero_division=0))
    roc_auc = float(roc_auc_score(y, y_prob))
    cm = confusion_matrix(y, y_pred).tolist()

    # 5-Fold Stratified Cross-Validation F1 (if sample size allows)
    try:
        skf = StratifiedKFold(n_splits=min(5, len(y) // 5), shuffle=True, random_state=RANDOM_STATE)
        cv_scores = cross_val_score(model, X, y, cv=skf, scoring="f1")
        cv_f1 = float(np.mean(cv_scores))
        cv_f1_std = float(np.std(cv_scores))
    except Exception:
        cv_f1 = f1
        cv_f1_std = 0.0

    return {
        "accuracy": round(accuracy, 4),
        "precision": round(precision, 4),
        "recall": round(recall, 4),
        "f1": round(f1, 4),
        "roc_auc": round(roc_auc, 4),
        "cv_f1": round(cv_f1, 4),
        "cv_f1_std": round(cv_f1_std, 4),
        "confusion_matrix": cm
    }


def compare_models(models_dir: str, data_path: str) -> dict:
    """
    Loads Model v1, Candidate Model v2, and common real evaluation dataset,
    runs evaluation, formats comparison reports, and produces a decision.
    """
    models_folder = Path(models_dir)
    v1_model_path = models_folder / "recommendation_model.joblib"
    v2_candidate_path = models_folder / "candidate_recommendation_model_v2.joblib"
    json_report_path = models_folder / "model_comparison_v1_v2.json"
    md_report_path = models_folder / "model_comparison_v1_v2.md"

    timestamp = datetime.datetime.now(datetime.timezone.utc).isoformat()

    # Check Model v1 existence
    if not v1_model_path.exists():
        raise FileNotFoundError(f"Active Model v1 file '{v1_model_path}' not found.")

    # Check Candidate Model v2 existence
    if not v2_candidate_path.exists():
        result = {
            "activeModel": "1.0",
            "candidateModel": "2.0-candidate",
            "decision": "INSUFFICIENT_DATA",
            "reason": "Candidate Model v2 ('candidate_recommendation_model_v2.joblib') has not been trained yet.",
            "evaluationDatasetSize": 0,
            "minF1ImprovementThreshold": MIN_F1_IMPROVEMENT,
            "timestamp": timestamp
        }
        write_reports(result, json_report_path, md_report_path)
        print_comparison_summary(result)
        return result

    # Check Evaluation Dataset existence & size
    csv_file = Path(data_path)
    if not csv_file.exists():
        result = {
            "activeModel": "1.0",
            "candidateModel": "2.0-candidate",
            "decision": "INSUFFICIENT_DATA",
            "reason": f"Evaluation dataset '{data_path}' does not exist. More real learner interactions are required before Model V2 can be fairly compared with Model V1.",
            "evaluationDatasetSize": 0,
            "minF1ImprovementThreshold": MIN_F1_IMPROVEMENT,
            "timestamp": timestamp
        }
        write_reports(result, json_report_path, md_report_path)
        print_comparison_summary(result)
        return result

    df = pd.read_csv(csv_file)
    if len(df) < MIN_EVAL_SAMPLES or df[TARGET_COLUMN].nunique() < 2:
        result = {
            "activeModel": "1.0",
            "candidateModel": "2.0-candidate",
            "decision": "INSUFFICIENT_DATA",
            "reason": f"Evaluation dataset size ({len(df)}) is less than minimum required ({MIN_EVAL_SAMPLES}) or target contains only 1 class. More real learner interactions are required before Model V2 can be fairly compared with Model V1.",
            "evaluationDatasetSize": len(df),
            "minF1ImprovementThreshold": MIN_F1_IMPROVEMENT,
            "timestamp": timestamp
        }
        write_reports(result, json_report_path, md_report_path)
        print_comparison_summary(result)
        return result

    # Load Models
    v1_model = joblib.load(v1_model_path)
    v2_model = joblib.load(v2_candidate_path)

    X = df[FEATURE_COLUMNS]
    y = df[TARGET_COLUMN]

    # Evaluate Both Models on Identical Examples
    v1_metrics = evaluate_model_on_data(v1_model, X, y)
    v2_metrics = evaluate_model_on_data(v2_model, X, y)

    f1_delta = round(v2_metrics["f1"] - v1_metrics["f1"], 4)
    roc_auc_delta = round(v2_metrics["roc_auc"] - v1_metrics["roc_auc"], 4)

    deltas = {
        "accuracy": round(v2_metrics["accuracy"] - v1_metrics["accuracy"], 4),
        "precision": round(v2_metrics["precision"] - v1_metrics["precision"], 4),
        "recall": round(v2_metrics["recall"] - v1_metrics["recall"], 4),
        "f1": f1_delta,
        "roc_auc": roc_auc_delta,
        "cv_f1": round(v2_metrics["cv_f1"] - v1_metrics["cv_f1"], 4)
    }

    # Apply Promotion Rules
    if f1_delta >= MIN_F1_IMPROVEMENT and roc_auc_delta >= -MAX_ROCAUC_DEGRADATION:
        decision = "PROMOTE_V2_CANDIDATE"
        reason = f"Candidate Model V2 improved F1 score by {f1_delta:+.4f} (>= {MIN_F1_IMPROVEMENT}) while maintaining comparable ROC-AUC ({roc_auc_delta:+.4f})."
    else:
        decision = "KEEP_V1"
        if f1_delta < MIN_F1_IMPROVEMENT:
            reason = f"Candidate Model V2 F1 improvement ({f1_delta:+.4f}) did not meet the required threshold ({MIN_F1_IMPROVEMENT})."
        else:
            reason = f"Candidate Model V2 ROC-AUC degraded significantly ({roc_auc_delta:+.4f} < -{MAX_ROCAUC_DEGRADATION})."

    result = {
        "activeModel": "1.0",
        "candidateModel": "2.0-candidate",
        "decision": decision,
        "reason": reason,
        "evaluationDatasetSize": len(df),
        "minF1ImprovementThreshold": MIN_F1_IMPROVEMENT,
        "modelV1Metrics": v1_metrics,
        "modelV2Metrics": v2_metrics,
        "metricDeltas": deltas,
        "timestamp": timestamp
    }

    write_reports(result, json_report_path, md_report_path)
    print_comparison_summary(result)
    return result


def write_reports(result: dict, json_path: Path, md_path: Path):
    """
    Writes JSON and Markdown comparison reports.
    """
    json_path.parent.mkdir(parents=True, exist_ok=True)

    # Write JSON report
    with open(json_path, "w") as f:
        json.dump(result, f, indent=2)

    # Write Markdown report
    md_content = f"""# Model V1 vs Model V2 Evaluation & Comparison Report

- **Date & Time**: `{result.get('timestamp')}`
- **Active Model Version**: `{result.get('activeModel')}`
- **Candidate Model Version**: `{result.get('candidateModel')}`
- **Evaluation Dataset Size**: `{result.get('evaluationDatasetSize')}`
- **Final Decision**: **`{result.get('decision')}`**
- **Decision Rationale**: {result.get('reason')}

---

## Metric Comparison Table

| Metric | Model V1 (Active) | Model V2 (Candidate) | Difference (V2 - V1) |
| :--- | :---: | :---: | :---: |
"""
    if "modelV1Metrics" in result and "modelV2Metrics" in result:
        v1 = result["modelV1Metrics"]
        v2 = result["modelV2Metrics"]
        d = result["metricDeltas"]
        md_content += f"""| **Accuracy** | {v1['accuracy']:.4f} | {v2['accuracy']:.4f} | {d['accuracy']:+.4f} |
| **Precision** | {v1['precision']:.4f} | {v2['precision']:.4f} | {d['precision']:+.4f} |
| **Recall** | {v1['recall']:.4f} | {v2['recall']:.4f} | {d['recall']:+.4f} |
| **F1 Score** | **{v1['f1']:.4f}** | **{v2['f1']:.4f}** | **{d['f1']:+.4f}** |
| **ROC-AUC** | {v1['roc_auc']:.4f} | {v2['roc_auc']:.4f} | {d['roc_auc']:+.4f} |
| **5-Fold CV F1** | {v1['cv_f1']:.4f} | {v2['cv_f1']:.4f} | {d['cv_f1']:+.4f} |

---

## Confusion Matrices

### Model V1 Confusion Matrix
```text
{v1['confusion_matrix']}
```

### Model V2 Confusion Matrix
```text
{v2['confusion_matrix']}
```
"""
    else:
        md_content += "| *N/A* | *Insufficient Real Interaction Data* | *N/A* | *N/A* |\n"

    md_content += """
---
> **Model Integrity Notice**: Production Model v1 (`recommendation_model.joblib`) remains the active production model. No automatic model promotion has occurred.
"""
    with open(md_path, "w") as f:
        f.write(md_content)


def print_comparison_summary(result: dict):
    """
    Prints clean, formatted comparison output to console.
    """
    print("=" * 60)
    print("MODEL V1 vs MODEL V2 FORMAL EVALUATION SUMMARY")
    print("=" * 60)
    print(f"Evaluation Dataset Size : {result.get('evaluationDatasetSize')}")
    print(f"Decision                : {result.get('decision')}")
    print(f"Reason                  : {result.get('reason')}")
    print("-" * 60)

    if "modelV1Metrics" in result and "modelV2Metrics" in result:
        v1 = result["modelV1Metrics"]
        v2 = result["modelV2Metrics"]
        d = result["metricDeltas"]

        print(f"{'Metric':<18} | {'Model V1':<10} | {'Model V2':<10} | {'Delta':<10}")
        print("-" * 60)
        print(f"{'Accuracy':<18} | {v1['accuracy']:<10.4f} | {v2['accuracy']:<10.4f} | {d['accuracy']:+10.4f}")
        print(f"{'Precision':<18} | {v1['precision']:<10.4f} | {v2['precision']:<10.4f} | {d['precision']:+10.4f}")
        print(f"{'Recall':<18} | {v1['recall']:<10.4f} | {v2['recall']:<10.4f} | {d['recall']:+10.4f}")
        print(f"{'F1 Score':<18} | {v1['f1']:<10.4f} | {v2['f1']:<10.4f} | {d['f1']:+10.4f}")
        print(f"{'ROC-AUC':<18} | {v1['roc_auc']:<10.4f} | {v2['roc_auc']:<10.4f} | {d['roc_auc']:+10.4f}")
        print(f"{'5-Fold CV F1':<18} | {v1['cv_f1']:<10.4f} | {v2['cv_f1']:<10.4f} | {d['cv_f1']:+10.4f}")

    print("=" * 60)


if __name__ == "__main__":
    base_dir = Path(__file__).resolve().parent.parent
    data_file = str(base_dir / "data" / "real_interaction_training_data.csv")
    models_folder = str(base_dir / "models")

    compare_models(models_folder, data_file)
