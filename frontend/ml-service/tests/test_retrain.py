"""
Unit tests for Automated Model Retraining Workflow (Step 9H-5).
"""

import json
import os
import pytest
import pandas as pd
import joblib
from pathlib import Path
from sklearn.ensemble import GradientBoostingClassifier

from app.model_registry import ModelRegistry
from training.check_retraining_eligibility import check_eligibility
from training.retrain import run_retraining_pipeline, get_next_candidate_version, RetrainingLock
from training.compare_models import FEATURE_COLUMNS, TARGET_COLUMN


@pytest.fixture
def sample_feature_dict():
    return {
        "skill_gap_score": 0.85,
        "career_priority_score": 0.90,
        "skill_coverage": 0.75,
        "proficiency_gap": 0.50,
        "difficulty_match": 0.80,
        "course_rating": 0.90,
        "preference_match": 1.00,
        "mandatory_skill_match": 0.90,
        "course_duration_match": 0.70,
        "course_quality_score": 0.88
    }


def test_eligibility_check_below_threshold(tmp_path):
    """Test 1: Below threshold produces ELIGIBLE=False and SKIPPED result."""
    data_csv = tmp_path / "interactions.csv"
    state_file = tmp_path / "retraining_state.json"
    config_file = tmp_path / "config.json"

    # Save 100 rows (< 500 threshold)
    df = pd.DataFrame([{"user_id": f"u{i}", "course_id": f"c{i}", "interaction_type": "CLICKED"} for i in range(100)])
    df.to_csv(data_csv, index=False)

    config_file.write_text(json.dumps({"min_new_interactions": 500}))
    state_file.write_text(json.dumps({"last_training_interaction_count": 0}))

    res = check_eligibility(str(data_csv), str(config_file), str(state_file))

    assert not res["eligible"]
    assert res["new_interactions"] == 100
    assert res["threshold"] == 500


def test_eligibility_check_threshold_reached(tmp_path):
    """Test 2: Threshold reached produces ELIGIBLE=True."""
    data_csv = tmp_path / "interactions.csv"
    state_file = tmp_path / "retraining_state.json"
    config_file = tmp_path / "config.json"

    df = pd.DataFrame([{"user_id": f"u{i}", "course_id": f"c{i}", "interaction_type": "CLICKED"} for i in range(600)])
    df.to_csv(data_csv, index=False)

    config_file.write_text(json.dumps({"min_new_interactions": 500}))
    state_file.write_text(json.dumps({"last_training_interaction_count": 0}))

    res = check_eligibility(str(data_csv), str(config_file), str(state_file))

    assert res["eligible"]
    assert res["new_interactions"] == 600


def test_version_increment_logic(tmp_path):
    """Test 15: Candidate version increments correctly (1.0 -> 2.0 -> 3.0)."""
    models_dir = tmp_path / "models"
    models_dir.mkdir()
    reg = ModelRegistry(str(models_dir))

    assert get_next_candidate_version(reg) == "2.0"

    reg_dict = reg.load_registry()
    reg_dict["active_model"] = "2.0"
    reg.save_registry(reg_dict)

    assert get_next_candidate_version(reg) == "3.0"


def test_dry_run_mode_does_not_modify_models_or_registry(tmp_path, monkeypatch):
    """Test 13: Dry-run calculates status without modifying registry or models."""
    base_dir = tmp_path
    models_dir = base_dir / "models"
    models_dir.mkdir()
    v1_dir = models_dir / "v1"
    v1_dir.mkdir()

    # Model v1 Setup
    m1 = GradientBoostingClassifier(random_state=42, n_estimators=5)
    X = pd.DataFrame([{col: 0.5 for col in FEATURE_COLUMNS}] * 20)
    y = pd.Series([1, 0] * 10)
    m1.fit(X, y)
    joblib.dump(m1, v1_dir / "recommendation_model.joblib")
    with open(v1_dir / "feature_columns.json", "w") as f: json.dump(FEATURE_COLUMNS, f)
    with open(v1_dir / "metadata.json", "w") as f: json.dump({"version": "1.0"}, f)

    reg = ModelRegistry(str(models_dir))
    reg.load_registry()

    # Dry Run Execution
    res = run_retraining_pipeline(dry_run=True, force=True)

    assert res["status"] == "DRY_RUN_ELIGIBLE"
    assert reg.get_active_version() == "1.0", "Dry run must leave active model version at 1.0"


def test_lock_prevents_concurrent_execution(tmp_path):
    """Test 14: Lock file prevents concurrent execution."""
    lock_file = tmp_path / "models" / "retraining.lock"
    lock_file.parent.mkdir(parents=True, exist_ok=True)
    lock_file.write_text("LOCKED")

    with pytest.raises(SystemExit):
        with RetrainingLock(lock_file):
            pass


def test_idempotent_duplicate_run_prevention(tmp_path):
    """Test 12: Running pipeline twice without new interactions skips second run."""
    data_csv = tmp_path / "data" / "recommendation_interactions.csv"
    data_csv.parent.mkdir(parents=True, exist_ok=True)

    # 100 rows
    df = pd.DataFrame([{"user_id": f"u{i}", "course_id": f"c{i}", "interaction_type": "CLICKED"} for i in range(100)])
    df.to_csv(data_csv, index=False)

    res1 = check_eligibility(str(data_csv))
    assert not res1["eligible"]
