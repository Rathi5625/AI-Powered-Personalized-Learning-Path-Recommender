"""
Candidate Recommendation Model V2 Training Script (Step 9H-2).

This script trains Candidate Model v2 using real user interaction data
from data/real_interaction_training_data.csv, evaluates metrics, and exports
candidate model artifacts without overwriting production Model v1.
"""

import json
import os
import sys
import datetime
from pathlib import Path
import pandas as pd
import numpy as np
import joblib

from sklearn.ensemble import GradientBoostingClassifier
from sklearn.model_selection import train_test_split, cross_val_score, StratifiedKFold, GroupShuffleSplit
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score, confusion_matrix, classification_report

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
MINIMUM_SAMPLES = 100
MIN_CLASS_SAMPLES = 10
RANDOM_STATE = 42


def validate_training_data(df: pd.DataFrame) -> tuple[bool, str]:
    """
    Validates data sufficiency, column presence, missing values, and class distribution.
    """
    if df.empty:
        return False, "Input dataframe is empty."

    # Check target
    if TARGET_COLUMN not in df.columns:
        return False, f"Missing target column '{TARGET_COLUMN}'."

    # Check features
    missing_features = [col for col in FEATURE_COLUMNS if col not in df.columns]
    if missing_features:
        return False, f"Missing feature columns: {missing_features}."

    # Check sample size
    if len(df) < MINIMUM_SAMPLES:
        return False, f"Dataset contains {len(df)} samples, which is less than minimum required ({MINIMUM_SAMPLES})."

    # Check NaN / Inf
    if df[FEATURE_COLUMNS].isna().sum().sum() > 0:
        return False, "Dataset contains NaN values."

    if np.isinf(df[FEATURE_COLUMNS].select_dtypes(include=[np.number])).sum().sum() > 0:
        return False, "Dataset contains infinite values."

    # Check class distribution
    class_counts = df[TARGET_COLUMN].value_counts()
    if len(class_counts) < 2:
        return False, "Target contains only 1 class. Both positive (1) and negative (0) examples required."

    pos_count = int(class_counts.get(1, 0))
    neg_count = int(class_counts.get(0, 0))

    if pos_count < MIN_CLASS_SAMPLES or neg_count < MIN_CLASS_SAMPLES:
        return False, f"Insufficient class distribution: Positive={pos_count}, Negative={neg_count}. Minimum required per class = {MIN_CLASS_SAMPLES}."

    return True, "Valid"


def train_candidate_model_v2(data_path: str, models_dir: str) -> dict:
    """
    Loads real interaction training data, validates requirements, trains Candidate Model v2,
    evaluates performance, and saves candidate artifacts.
    """
    csv_path = Path(data_path)
    if not csv_path.exists():
        print("Insufficient real interaction data for Model v2.")
        print(f"Reason: File '{data_path}' does not exist.")
        return {"status": "file_not_found"}

    df = pd.read_csv(csv_path)
    is_valid, reason = validate_training_data(df)

    if not is_valid:
        print("Insufficient real interaction data for Model v2.")
        print(f"Reason: {reason}")
        return {"status": "validation_failed", "reason": reason}

    X = df[FEATURE_COLUMNS]
    y = df[TARGET_COLUMN]

    # Perform Train/Test Split (80/20)
    # Prefer user-aware group split if user_id is available and has sufficient distinct users
    user_aware_used = False
    if "user_id" in df.columns and df["user_id"].nunique() >= 10:
        try:
            gss = GroupShuffleSplit(n_splits=1, test_size=0.20, random_state=RANDOM_STATE)
            train_idx, test_idx = next(gss.split(X, y, df["user_id"]))
            X_train, X_test = X.iloc[train_idx], X.iloc[test_idx]
            y_train, y_test = y.iloc[train_idx], y.iloc[test_idx]
            user_aware_used = True
        except Exception:
            user_aware_used = False

    if not user_aware_used:
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.20, random_state=RANDOM_STATE, stratify=y
        )

    # Train Candidate Model v2
    model = GradientBoostingClassifier(random_state=RANDOM_STATE)
    model.fit(X_train, y_train)

    # Predictions & Probabilities
    y_pred = model.predict(X_test)
    y_prob = model.predict_proba(X_test)[:, 1]

    # Calculate Evaluation Metrics
    accuracy = float(accuracy_score(y_test, y_pred))
    precision = float(precision_score(y_test, y_pred, zero_division=0))
    recall = float(recall_score(y_test, y_pred, zero_division=0))
    f1 = float(f1_score(y_test, y_pred, zero_division=0))
    roc_auc = float(roc_auc_score(y_test, y_prob))

    # Perform 5-Fold Cross-Validation
    skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)
    cv_scores = cross_val_score(model, X, y, cv=skf, scoring="f1")
    cv_f1 = float(np.mean(cv_scores))
    cv_f1_std = float(np.std(cv_scores))

    # Print Model Evaluation Report
    print("=" * 60)
    print("CANDIDATE MODEL V2 EVALUATION REPORT")
    print("=" * 60)
    print(f"Data Split Strategy : {'User-Aware Group Split' if user_aware_used else 'Stratified Train-Test Split'}")
    print(f"Training Samples    : {len(X_train)}")
    print(f"Testing Samples     : {len(X_test)}")
    print("-" * 60)
    print(f"Accuracy            : {accuracy:.4f}")
    print(f"Precision           : {precision:.4f}")
    print(f"Recall              : {recall:.4f}")
    print(f"F1 Score            : {f1:.4f}")
    print(f"ROC-AUC             : {roc_auc:.4f}")
    print(f"5-Fold CV F1        : {cv_f1:.4f} +/- {cv_f1_std:.4f}")
    print("-" * 60)
    print("Confusion Matrix:")
    print(confusion_matrix(y_test, y_pred))
    print("-" * 60)
    print("Classification Report:")
    print(classification_report(y_test, y_pred, zero_division=0))
    print("-" * 60)

    # Calculate & Print Feature Importances
    importances = model.feature_importances_
    feat_imp = pd.DataFrame({
        "feature": FEATURE_COLUMNS,
        "importance": importances
    }).sort_values("importance", ascending=False)

    print("Feature Importances (Sorted Descending):")
    for _, row in feat_imp.iterrows():
        print(f"  {row['feature']:<25} : {row['importance'] * 100:.2f}%")
    print("=" * 60)

    # Save Candidate Artifacts
    out_dir = Path(models_dir)
    out_dir.mkdir(parents=True, exist_ok=True)

    candidate_model_path = out_dir / "candidate_recommendation_model_v2.joblib"
    candidate_features_path = out_dir / "candidate_feature_columns_v2.json"
    candidate_metadata_path = out_dir / "candidate_model_v2_metadata.json"
    prod_model_path = out_dir / "recommendation_model.joblib"

    # Save Model v2 Candidate
    joblib.dump(model, candidate_model_path)

    # Save Feature Columns
    with open(candidate_features_path, "w") as f:
        json.dump(FEATURE_COLUMNS, f, indent=2)

    # Save Candidate Metadata
    metadata = {
        "model_version": "2.0-candidate",
        "model_type": "GradientBoostingClassifier",
        "dataset_name": "real_interaction_training_data.csv",
        "dataset_size": len(df),
        "training_samples": len(X_train),
        "test_samples": len(X_test),
        "feature_names": FEATURE_COLUMNS,
        "random_state": RANDOM_STATE,
        "accuracy": round(accuracy, 4),
        "precision": round(precision, 4),
        "recall": round(recall, 4),
        "f1": round(f1, 4),
        "roc_auc": round(roc_auc, 4),
        "cv_f1": round(cv_f1, 4),
        "cv_f1_std": round(cv_f1_std, 4),
        "training_timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "data_source": "real_user_interactions"
    }

    with open(candidate_metadata_path, "w") as f:
        json.dump(metadata, f, indent=2)

    print("\nSaved Candidate Artifacts:")
    print(f"  Model File   : {candidate_model_path}")
    print(f"  Features File: {candidate_features_path}")
    print(f"  Metadata File: {candidate_metadata_path}")
    print("\nPROMOTION NOTICE: Production Model v1 ('recommendation_model.joblib') remains active and unchanged.")

    return metadata


if __name__ == "__main__":
    base_dir = Path(__file__).resolve().parent.parent
    data_file = str(base_dir / "data" / "real_interaction_training_data.csv")
    models_folder = str(base_dir / "models")

    train_candidate_model_v2(data_file, models_folder)
