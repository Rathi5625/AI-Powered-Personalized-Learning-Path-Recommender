"""
Unit tests for Model Registry, Versioning, Promotion, and FastAPI Routes (Step 9H-4).
"""

import json
import pytest
import pandas as pd
import joblib
from pathlib import Path
from fastapi.testclient import TestClient
from sklearn.ensemble import GradientBoostingClassifier

from app.main import app
from app.model_registry import ModelRegistry
from app.model_service import model_service
from training.compare_models import FEATURE_COLUMNS, TARGET_COLUMN

client = TestClient(app)

@pytest.fixture
def temp_registry_env(tmp_path):
    models_dir = tmp_path / "models"
    models_dir.mkdir()
    v1_dir = models_dir / "v1"
    v2_dir = models_dir / "v2"
    v1_dir.mkdir()
    v2_dir.mkdir()

    # Train & save dummy v1 model
    m1 = GradientBoostingClassifier(random_state=42, n_estimators=5)
    X = pd.DataFrame([{col: 0.5 for col in FEATURE_COLUMNS}] * 20)
    y = pd.Series([1, 0] * 10)
    m1.fit(X, y)

    v1_model_p = v1_dir / "recommendation_model.joblib"
    v1_feat_p = v1_dir / "feature_columns.json"
    v1_meta_p = v1_dir / "metadata.json"

    joblib.dump(m1, v1_model_p)
    with open(v1_feat_p, "w") as f:
        json.dump(FEATURE_COLUMNS, f)
    with open(v1_meta_p, "w") as f:
        json.dump({"version": "1.0", "accuracy": 0.80, "f1": 0.7614}, f)

    # Save registry.json
    reg_p = models_dir / "registry.json"
    initial_reg = {
        "active_model": "1.0",
        "models": [
            {
                "version": "1.0",
                "status": "ACTIVE",
                "model_path": str(v1_model_p),
                "features_path": str(v1_feat_p),
                "metadata_path": str(v1_meta_p),
                "f1": 0.7614,
                "roc_auc": 0.8862
            }
        ]
    }
    with open(reg_p, "w") as f:
        json.dump(initial_reg, f)

    return ModelRegistry(str(models_dir)), models_dir, v1_dir, v2_dir, X, y

def test_registry_loads_and_gets_active_model(temp_registry_env):
    """Test 1 & 2: Registry loads cleanly and resolves active model artifacts."""
    registry, _, _, _, _, _ = temp_registry_env
    assert registry.get_active_version() == "1.0"

    model, features, meta = registry.get_active_model_artifacts()
    assert model is not None
    assert features == FEATURE_COLUMNS
    assert meta["version"] == "1.0"

def test_candidate_registration(temp_registry_env):
    """Test 4: Candidate model registration adds entry with CANDIDATE status."""
    registry, models_dir, _, v2_dir, X, y = temp_registry_env

    # Train v2
    m2 = GradientBoostingClassifier(random_state=42, n_estimators=10)
    m2.fit(X, y)
    v2_model_p = v2_dir / "recommendation_model.joblib"
    v2_feat_p = v2_dir / "feature_columns.json"
    v2_meta_p = v2_dir / "metadata.json"

    joblib.dump(m2, v2_model_p)
    with open(v2_feat_p, "w") as f: json.dump(FEATURE_COLUMNS, f)
    with open(v2_meta_p, "w") as f: json.dump({"version": "2.0", "f1": 0.82}, f)

    entry = registry.register_candidate("2.0", str(v2_model_p), str(v2_feat_p), str(v2_meta_p), {"f1": 0.82})

    assert entry["status"] == "CANDIDATE"
    info = registry.get_model_info("2.0")
    assert info["version"] == "2.0"
    assert info["status"] == "CANDIDATE"

def test_successful_promotion_and_archiving(temp_registry_env):
    """Test 5, 6 & 7: Successful promotion archives previous model and maintains single active model."""
    registry, models_dir, _, v2_dir, X, y = temp_registry_env

    # Setup v2 artifacts
    m2 = GradientBoostingClassifier(random_state=42, n_estimators=10)
    m2.fit(X, y)
    v2_model_p = v2_dir / "recommendation_model.joblib"
    v2_feat_p = v2_dir / "feature_columns.json"
    v2_meta_p = v2_dir / "metadata.json"

    joblib.dump(m2, v2_model_p)
    with open(v2_feat_p, "w") as f: json.dump(FEATURE_COLUMNS, f)
    with open(v2_meta_p, "w") as f: json.dump({"version": "2.0", "f1": 0.82}, f)

    registry.register_candidate("2.0", str(v2_model_p), str(v2_feat_p), str(v2_meta_p), {"f1": 0.82})

    # Setup approved decision file
    comp_p = models_dir / "model_comparison_v1_v2.json"
    with open(comp_p, "w") as f:
        json.dump({"decision": "PROMOTE_V2_CANDIDATE"}, f)

    res = registry.promote_model("2.0")

    assert res["status"] == "promoted"
    assert registry.get_active_version() == "2.0"

    models = registry.list_models()
    v1_info = [m for m in models if m["version"] == "1.0"][0]
    v2_info = [m for m in models if m["version"] == "2.0"][0]

    assert v1_info["status"] == "ARCHIVED", "Previous model must become ARCHIVED"
    assert v2_info["status"] == "ACTIVE", "New promoted model must be ACTIVE"

    active_count = sum(1 for m in models if m["status"] == "ACTIVE")
    assert active_count == 1, "Exactly ONE model can be ACTIVE"

def test_rejected_candidate_cannot_be_promoted(temp_registry_env):
    """Test 8: Rejected candidate cannot be promoted."""
    registry, _, _, _, _, _ = temp_registry_env

    reg = registry.load_registry()
    reg["models"].append({"version": "3.0", "status": "REJECTED", "model_path": "dummy", "features_path": "dummy", "metadata_path": "dummy"})
    registry.save_registry(reg)

    with pytest.raises(ValueError, match="REJECTED"):
        registry.promote_model("3.0")

def test_missing_artifacts_prevent_promotion(temp_registry_env):
    """Test 9 & 10: Missing artifact or corrupt binary prevents promotion without altering registry."""
    registry, models_dir, _, _, _, _ = temp_registry_env

    reg = registry.load_registry()
    reg["models"].append({
        "version": "4.0",
        "status": "CANDIDATE",
        "model_path": str(models_dir / "missing.joblib"),
        "features_path": str(models_dir / "missing.json"),
        "metadata_path": str(models_dir / "missing_meta.json")
    })
    registry.save_registry(reg)

    with pytest.raises(FileNotFoundError):
        registry.promote_model("4.0")

    assert registry.get_active_version() == "1.0", "Failed promotion must not corrupt active model"

def test_rollback_mechanism(temp_registry_env):
    """Test 11: Rollback re-activates previous model."""
    registry, models_dir, _, v2_dir, X, y = temp_registry_env

    m2 = GradientBoostingClassifier(random_state=42, n_estimators=10)
    m2.fit(X, y)
    v2_model_p = v2_dir / "recommendation_model.joblib"
    v2_feat_p = v2_dir / "feature_columns.json"
    v2_meta_p = v2_dir / "metadata.json"

    joblib.dump(m2, v2_model_p)
    with open(v2_feat_p, "w") as f: json.dump(FEATURE_COLUMNS, f)
    with open(v2_meta_p, "w") as f: json.dump({"version": "2.0"}, f)

    registry.register_candidate("2.0", str(v2_model_p), str(v2_feat_p), str(v2_meta_p), {})

    with open(models_dir / "model_comparison_v1_v2.json", "w") as f:
        json.dump({"decision": "PROMOTE_V2_CANDIDATE"}, f)

    registry.promote_model("2.0")
    assert registry.get_active_version() == "2.0"

    # Rollback to 1.0
    res = registry.rollback_model("1.0")
    assert res["status"] == "rolled_back"
    assert registry.get_active_version() == "1.0"

def test_fastapi_health_model_info_and_predict():
    """Test 3, 13 & 14: FastAPI endpoints return active model info and contract."""
    with TestClient(app) as test_client:
        response_health = test_client.get("/health")
        assert response_health.status_code == 200
        data_health = response_health.json()
        assert data_health["status"] == "UP"
        assert "active_model" in data_health

        response_models = test_client.get("/models")
        assert response_models.status_code == 200
        assert "active_model" in response_models.json()

        # Predict call with valid [0.0, 1.0] feature values
        payload = {col: 0.5 for col in FEATURE_COLUMNS}
        payload["course_rating"] = 0.90
        response_pred = test_client.post("/predict", json=payload)
        assert response_pred.status_code == 200
        pred_data = response_pred.json()
        assert "recommendation_probability" in pred_data
        assert "recommendation_score" in pred_data
        assert "model_version" in pred_data
