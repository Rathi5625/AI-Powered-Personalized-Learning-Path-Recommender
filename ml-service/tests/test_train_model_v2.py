"""
Unit tests for Candidate Model V2 Training Pipeline (Step 9H-2).
"""

import json
import os
import pytest
import pandas as pd
import numpy as np
from pathlib import Path
from training.train_model_v2 import validate_training_data, train_candidate_model_v2, FEATURE_COLUMNS, TARGET_COLUMN

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

def test_validate_training_data_success(sample_feature_dict):
    """Test 1: Dataset validation succeeds for a valid dataset."""
    valid_data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", TARGET_COLUMN: 1 if i % 2 == 0 else 0, **sample_feature_dict}
        for i in range(120)
    ]
    df = pd.DataFrame(valid_data)
    is_valid, reason = validate_training_data(df)

    assert is_valid
    assert reason == "Valid"

def test_missing_feature_detection(sample_feature_dict):
    """Test 2: Missing feature column is detected."""
    incomplete_features = sample_feature_dict.copy()
    del incomplete_features["skill_gap_score"]

    data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", TARGET_COLUMN: 1 if i % 2 == 0 else 0, **incomplete_features}
        for i in range(120)
    ]
    df = pd.DataFrame(data)
    is_valid, reason = validate_training_data(df)

    assert not is_valid
    assert "Missing feature columns" in reason

def test_missing_target_detection(sample_feature_dict):
    """Test 3: Missing recommendation_label column is detected."""
    data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", **sample_feature_dict}
        for i in range(120)
    ]
    df = pd.DataFrame(data)
    is_valid, reason = validate_training_data(df)

    assert not is_valid
    assert f"Missing target column '{TARGET_COLUMN}'" in reason

def test_insufficient_sample_detection(sample_feature_dict):
    """Test 4: Insufficient sample count (<100) or class imbalance is detected."""
    small_data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", TARGET_COLUMN: 1 if i % 2 == 0 else 0, **sample_feature_dict}
        for i in range(50) # 50 < 100
    ]
    df = pd.DataFrame(small_data)
    is_valid, reason = validate_training_data(df)

    assert not is_valid
    assert "less than minimum required (100)" in reason

def test_train_candidate_model_v2_and_artifacts(tmp_path, sample_feature_dict):
    """Test 5, 6, 7, 8, 9: Successful training creates candidate artifacts without overwriting Model v1."""
    data_dir = tmp_path / "data"
    models_dir = tmp_path / "models"
    data_dir.mkdir()
    models_dir.mkdir()

    # Create dummy Model v1 to verify it is NOT touched
    model_v1_path = models_dir / "recommendation_model.joblib"
    model_v1_path.write_text("DUMMY_MODEL_V1_CONTENT")
    v1_mod_time = model_v1_path.stat().st_mtime

    # Generate 120 valid training samples
    valid_data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", TARGET_COLUMN: 1 if i % 2 == 0 else 0, **sample_feature_dict}
        for i in range(120)
    ]
    csv_file = data_dir / "real_interaction_training_data.csv"
    pd.DataFrame(valid_data).to_csv(csv_file, index=False)

    # Execute candidate model training
    metadata = train_candidate_model_v2(str(csv_file), str(models_dir))

    # Verify Candidate Artifact Creation
    candidate_model_file = models_dir / "candidate_recommendation_model_v2.joblib"
    candidate_features_file = models_dir / "candidate_feature_columns_v2.json"
    candidate_metadata_file = models_dir / "candidate_model_v2_metadata.json"

    assert candidate_model_file.exists(), "Candidate model v2 file must be created"
    assert candidate_features_file.exists(), "Candidate feature columns JSON must be created"
    assert candidate_metadata_file.exists(), "Candidate metadata JSON must be created"

    # Verify Model v1 was NOT overwritten
    assert model_v1_path.read_text() == "DUMMY_MODEL_V1_CONTENT", "Model v1 must not be modified or overwritten"
    assert model_v1_path.stat().st_mtime == v1_mod_time, "Model v1 modification timestamp must be unchanged"

    # Verify Feature Order in JSON
    with open(candidate_features_file) as f:
        saved_features = json.load(f)
    assert saved_features == FEATURE_COLUMNS, "Saved feature order must match training feature columns"

    # Verify Metadata Contents
    with open(candidate_metadata_file) as f:
        saved_metadata = json.load(f)
    assert saved_metadata["model_version"] == "2.0-candidate"
    assert saved_metadata["dataset_size"] == 120
    assert "accuracy" in saved_metadata
    assert "f1" in saved_metadata
    assert "roc_auc" in saved_metadata
