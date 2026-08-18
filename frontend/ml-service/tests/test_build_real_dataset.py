"""
Unit tests for Real Interaction Dataset Construction Pipeline (Step 9H-1).
"""

import pytest
import pandas as pd
import numpy as np
from pathlib import Path
from training.build_real_dataset import (
    consolidate_user_course_interactions,
    validate_dataset_quality,
    build_real_dataset,
    FEATURE_COLUMNS
)

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

def test_interaction_grouping_and_positive_consolidation(sample_feature_dict):
    """Test 1 & 2: Multiple interactions for single (user, course) are grouped and positive signals yield label = 1."""
    data = [
        {"user_id": "u1", "course_id": "c1", "interaction_type": "VIEWED", **sample_feature_dict},
        {"user_id": "u1", "course_id": "c1", "interaction_type": "CLICKED", **sample_feature_dict},
        {"user_id": "u1", "course_id": "c1", "interaction_type": "STARTED", **sample_feature_dict},
        {"user_id": "u1", "course_id": "c1", "interaction_type": "COMPLETED", **sample_feature_dict},
    ]
    df = pd.DataFrame(data)
    consolidated = consolidate_user_course_interactions(df)

    assert len(consolidated) == 1, "Multiple interaction events for same user-course pair must group into 1 row"
    assert consolidated.iloc[0]["recommendation_label"] == 1, "Journey ending in COMPLETED must yield label 1"

def test_negative_interaction_consolidation(sample_feature_dict):
    """Test 3: SKIPPED interaction yields label = 0."""
    data = [
        {"user_id": "u2", "course_id": "c2", "interaction_type": "VIEWED", **sample_feature_dict},
        {"user_id": "u2", "course_id": "c2", "interaction_type": "SKIPPED", **sample_feature_dict},
    ]
    df = pd.DataFrame(data)
    consolidated = consolidate_user_course_interactions(df)

    assert len(consolidated) == 1
    assert consolidated.iloc[0]["recommendation_label"] == 0, "SKIPPED journey must yield label 0"

def test_viewed_only_handling(sample_feature_dict):
    """Test 4: VIEWED-only interaction is excluded due to insufficient signal."""
    data = [
        {"user_id": "u3", "course_id": "c3", "interaction_type": "VIEWED", **sample_feature_dict},
    ]
    df = pd.DataFrame(data)
    consolidated = consolidate_user_course_interactions(df)

    assert len(consolidated) == 0, "VIEWED-only journey must be excluded"

def test_clicked_handling(sample_feature_dict):
    """Test 5: CLICKED interaction yields weak positive label = 1."""
    data = [
        {"user_id": "u4", "course_id": "c4", "interaction_type": "CLICKED", **sample_feature_dict},
    ]
    df = pd.DataFrame(data)
    consolidated = consolidate_user_course_interactions(df)

    assert len(consolidated) == 1
    assert consolidated.iloc[0]["recommendation_label"] == 1, "CLICKED journey must yield label 1"

def test_duplicate_removal_after_grouping(sample_feature_dict):
    """Test 6: Consolidated dataset has zero duplicate (user_id, course_id) pairs."""
    data = [
        {"user_id": "u1", "course_id": "c1", "interaction_type": "STARTED", **sample_feature_dict},
        {"user_id": "u1", "course_id": "c1", "interaction_type": "COMPLETED", **sample_feature_dict},
        {"user_id": "u2", "course_id": "c2", "interaction_type": "SKIPPED", **sample_feature_dict},
    ]
    df = pd.DataFrame(data)
    consolidated = consolidate_user_course_interactions(df)

    duplicates = consolidated.duplicated(subset=["user_id", "course_id"]).sum()
    assert duplicates == 0, "Consolidated output must contain zero duplicates"

def test_missing_feature_detection():
    """Test 7: Missing feature column is detected during dataset quality validation."""
    data = [
        {"user_id": "u1", "course_id": "c1", "interaction_type": "STARTED", "skill_gap_score": 0.8, "recommendation_label": 1}
    ]
    df = pd.DataFrame(data)
    report = validate_dataset_quality(df)

    assert not report["is_valid"]
    assert "Missing required feature columns" in report["errors"][0]

def test_invalid_feature_range_detection(sample_feature_dict):
    """Test 8: Out of bounds feature values (<0 or >1) are flagged."""
    invalid_features = sample_feature_dict.copy()
    invalid_features["skill_gap_score"] = 1.50 # Invalid > 1.0

    data = [
        {"user_id": "u1", "course_id": "c1", "interaction_type": "STARTED", "recommendation_label": 1, **invalid_features},
        {"user_id": "u2", "course_id": "c2", "interaction_type": "SKIPPED", "recommendation_label": 0, **sample_feature_dict}
    ]
    df = pd.DataFrame(data)
    report = validate_dataset_quality(df)

    assert not report["is_valid"]
    assert any("outside [0.0, 1.0]" in err for err in report["errors"])

def test_insufficient_data_detection(tmp_path, sample_feature_dict):
    """Test 9: Small interaction CSV (<100 samples) triggers insufficient data reporting."""
    small_data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", "interaction_type": "STARTED" if i % 2 == 0 else "SKIPPED", **sample_feature_dict}
        for i in range(10) # 10 samples < 100 min threshold
    ]
    input_file = tmp_path / "small_interactions.csv"
    output_file = tmp_path / "real_interaction_training_data.csv"
    pd.DataFrame(small_data).to_csv(input_file, index=False)

    df_out, report = build_real_dataset(str(input_file), str(output_file), min_samples=100)

    assert df_out is None, "Dataset output must be None when samples < threshold"
    assert report["status"] == "insufficient_data"
    assert not output_file.exists(), "Output CSV file must not be created on insufficient data"

def test_successful_dataset_generation_when_sufficient(tmp_path, sample_feature_dict):
    """Test 10: Sufficient interactions (>=100 samples) creates real_interaction_training_data.csv."""
    large_data = [
        {"user_id": f"u{i}", "course_id": f"c{i}", "interaction_type": "STARTED" if i % 2 == 0 else "SKIPPED", **sample_feature_dict}
        for i in range(120) # 120 samples >= 100 threshold
    ]
    input_file = tmp_path / "sufficient_interactions.csv"
    output_file = tmp_path / "real_interaction_training_data.csv"
    pd.DataFrame(large_data).to_csv(input_file, index=False)

    df_out, report = build_real_dataset(str(input_file), str(output_file), min_samples=100)

    assert df_out is not None
    assert output_file.exists()
    assert len(df_out) == 120
    assert "recommendation_label" in df_out.columns
    assert set(df_out["recommendation_label"].unique()) == {0, 1}
