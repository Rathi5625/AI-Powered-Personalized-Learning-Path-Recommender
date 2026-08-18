"""
Unit tests for Model V1 vs Model V2 Evaluation & Comparison Pipeline (Step 9H-3).
"""

import json
import pytest
import pandas as pd
import numpy as np
import joblib
from pathlib import Path
from sklearn.ensemble import GradientBoostingClassifier
from training.compare_models import compare_models, evaluate_model_on_data, FEATURE_COLUMNS, TARGET_COLUMN

@pytest.fixture
def sample_feature_dict():
    return {
        "skill_gap_score": 0.85,
        "career_priority_score": 0.90,
        "skill_coverage": 0.75,
        "proficiency_gap": 0.50,
        "difficulty_match": 0.80,
        "course_rating": 4.50,
        "preference_match": 1.00,
        "mandatory_skill_match": 0.90,
        "course_duration_match": 0.70,
        "course_quality_score": 0.88
    }

def test_model_loading_and_prediction(sample_feature_dict):
    """Test 1, 4 & 5: Both models produce predictions and evaluation metrics on feature matrix."""
    df = pd.DataFrame([
        {"user_id": f"u{i}", "course_id": f"c{i}", TARGET_COLUMN: 1 if i % 2 == 0 else 0, **sample_feature_dict}
        for i in range(120)
    ])
    X = df[FEATURE_COLUMNS]
    y = df[TARGET_COLUMN]

    model = GradientBoostingClassifier(random_state=42)
    model.fit(X, y)

    metrics = evaluate_model_on_data(model, X, y)

    assert "accuracy" in metrics
    assert "f1" in metrics
    assert "roc_auc" in metrics
    assert "confusion_matrix" in metrics
    assert 0.0 <= metrics["accuracy"] <= 1.0

def test_insufficient_data_decision_path(tmp_path):
    """Test 3 & INSUFFICIENT_DATA decision: Missing evaluation dataset produces INSUFFICIENT_DATA decision."""
    models_dir = tmp_path / "models"
    data_dir = tmp_path / "data"
    models_dir.mkdir()
    data_dir.mkdir()

    # Create dummy Model v1 and Candidate Model v2
    dummy_v1 = models_dir / "recommendation_model.joblib"
    dummy_v2 = models_dir / "candidate_recommendation_model_v2.joblib"
    dummy_v1.write_text("V1_DUMMY")
    dummy_v2.write_text("V2_DUMMY")

    # Missing dataset scenario
    missing_data_path = data_dir / "non_existent.csv"
    res = compare_models(str(models_dir), str(missing_data_path))

    assert res["decision"] == "INSUFFICIENT_DATA"
    assert "does not exist" in res["reason"]

def test_keep_v1_decision_when_candidate_does_not_improve_f1(tmp_path, sample_feature_dict):
    """Test 6, 8, 9 & 11: KEEP_V1 decision when candidate F1 does not meet threshold."""
    models_dir = tmp_path / "models"
    data_dir = tmp_path / "data"
    models_dir.mkdir()
    data_dir.mkdir()

    # 120 dataset samples
    data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", TARGET_COLUMN: 1 if i % 2 == 0 else 0, **sample_feature_dict}
        for i in range(120)
    ]
    csv_file = data_dir / "real_interaction_training_data.csv"
    df = pd.DataFrame(data)
    df.to_csv(csv_file, index=False)

    X = df[FEATURE_COLUMNS]
    y = df[TARGET_COLUMN]

    # Train Model v1 (Stronger)
    m1 = GradientBoostingClassifier(random_state=42, n_estimators=100)
    m1.fit(X, y)
    v1_path = models_dir / "recommendation_model.joblib"
    joblib.dump(m1, v1_path)
    v1_mtime = v1_path.stat().st_mtime

    # Train Model v2 (Weaker candidate)
    m2 = GradientBoostingClassifier(random_state=42, n_estimators=10, max_depth=1)
    m2.fit(X, y)
    v2_path = models_dir / "candidate_recommendation_model_v2.joblib"
    joblib.dump(m2, v2_path)

    res = compare_models(str(models_dir), str(csv_file))

    assert res["decision"] == "KEEP_V1"
    assert v1_path.stat().st_mtime == v1_mtime, "Model v1 file must not be modified during evaluation"

def test_promote_v2_candidate_decision_when_candidate_improves_f1(tmp_path, sample_feature_dict):
    """Test 7, 8 & 10: PROMOTE_V2_CANDIDATE decision when Candidate v2 meaningfully improves F1 score."""
    models_dir = tmp_path / "models"
    data_dir = tmp_path / "data"
    models_dir.mkdir()
    data_dir.mkdir()

    # Create dataset where feature predicts target
    data = []
    for i in range(150):
        label = 1 if i % 2 == 0 else 0
        feat = sample_feature_dict.copy()
        feat["skill_gap_score"] = 0.95 if label == 1 else 0.05
        data.append({"user_id": f"u{i}", "course_id": f"c{i}", TARGET_COLUMN: label, **feat})

    csv_file = data_dir / "real_interaction_training_data.csv"
    df = pd.DataFrame(data)
    df.to_csv(csv_file, index=False)

    X = df[FEATURE_COLUMNS]
    y = df[TARGET_COLUMN]

    # Model v1: Poor model trained on inverted labels
    m1 = GradientBoostingClassifier(random_state=42, n_estimators=10, max_depth=1)
    m1.fit(X, 1 - y)
    joblib.dump(m1, models_dir / "recommendation_model.joblib")

    # Model v2: Strong model trained on correct labels
    m2 = GradientBoostingClassifier(random_state=42, n_estimators=50, max_depth=3)
    m2.fit(X, y)
    joblib.dump(m2, models_dir / "candidate_recommendation_model_v2.joblib")

    res = compare_models(str(models_dir), str(csv_file))

    assert res["decision"] == "PROMOTE_V2_CANDIDATE"
    assert res["metricDeltas"]["f1"] >= 0.01
    assert (models_dir / "model_comparison_v1_v2.json").exists()
    assert (models_dir / "model_comparison_v1_v2.md").exists()
