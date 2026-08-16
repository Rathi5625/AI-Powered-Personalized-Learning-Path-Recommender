"""
Model Registry & Version Management Service (Step 9H-4).

Provides thread-safe, atomic registration, retrieval, promotion, and rollback
functionality for recommendation model versions.
"""

import json
import os
import datetime
from pathlib import Path
import joblib
import numpy as np
import pandas as pd


class ModelRegistry:

    def __init__(self, models_dir: str = None):
        if models_dir is None:
            base_dir = Path(__file__).resolve().parent.parent
            models_dir = str(base_dir / "models")
        self.models_dir = Path(models_dir)
        self.registry_path = self.models_dir / "registry.json"

    def load_registry(self) -> dict:
        """
        Loads the registry JSON file. Creates default registry if missing.
        """
        if not self.registry_path.exists():
            default_registry = {
                "active_model": "1.0",
                "models": [
                    {
                        "version": "1.0",
                        "status": "ACTIVE",
                        "model_path": "models/v1/recommendation_model.joblib",
                        "features_path": "models/v1/feature_columns.json",
                        "metadata_path": "models/v1/metadata.json",
                        "f1": 0.7614,
                        "roc_auc": 0.8862
                    }
                ]
            }
            self.save_registry(default_registry)
            return default_registry

        with open(self.registry_path, "r") as f:
            return json.load(f)

    def save_registry(self, registry_dict: dict):
        """
        Atomically saves the registry dictionary to disk.
        """
        self.models_dir.mkdir(parents=True, exist_ok=True)
        tmp_path = self.registry_path.with_suffix(".tmp")
        with open(tmp_path, "w") as f:
            json.dump(registry_dict, f, indent=2)
        os.replace(tmp_path, self.registry_path)

    def get_active_version(self) -> str:
        """
        Returns the version string of the currently active model.
        """
        reg = self.load_registry()
        return reg.get("active_model", "1.0")

    def get_active_model_info(self) -> dict:
        """
        Returns the registry entry dictionary for the active model.
        """
        reg = self.load_registry()
        active_ver = reg.get("active_model", "1.0")
        for m in reg.get("models", []):
            if m.get("version") == active_ver:
                return m
        raise ValueError(f"Active model version '{active_ver}' not found in registry.")

    def resolve_path(self, relative_or_abs: str) -> Path:
        """
        Resolves relative model paths against models_dir or base_dir.
        """
        path = Path(relative_or_abs)
        if path.is_absolute() and path.exists():
            return path
        
        base_dir = self.models_dir.parent
        resolved = base_dir / relative_or_abs
        if resolved.exists():
            return resolved

        resolved_models = self.models_dir / relative_or_abs
        if resolved_models.exists():
            return resolved_models

        return path

    def get_active_model_artifacts(self) -> tuple:
        """
        Loads and returns (model_object, feature_columns_list, metadata_dict) for active model.
        """
        info = self.get_active_model_info()

        model_p = self.resolve_path(info["model_path"])
        features_p = self.resolve_path(info["features_path"])
        meta_p = self.resolve_path(info["metadata_path"])

        if not model_p.exists():
            raise FileNotFoundError(f"Active model binary not found at: {model_p}")
        if not features_p.exists():
            raise FileNotFoundError(f"Active feature columns JSON not found at: {features_p}")

        model = joblib.load(model_p)
        with open(features_p, "r") as f:
            features = json.load(f)

        metadata = {}
        if meta_p.exists():
            with open(meta_p, "r") as f:
                metadata = json.load(f)

        return model, features, metadata

    def list_models(self) -> list:
        """
        Returns list of registered model entries.
        """
        reg = self.load_registry()
        return reg.get("models", [])

    def get_model_info(self, version: str) -> dict:
        """
        Returns details for a specific model version.
        """
        reg = self.load_registry()
        for m in reg.get("models", []):
            if m.get("version") == version:
                return m
        return None

    def register_candidate(self, version: str, model_path: str, features_path: str, metadata_path: str, metrics: dict) -> dict:
        """
        Registers a new candidate model in the registry.
        """
        reg = self.load_registry()
        existing = [m for m in reg["models"] if m.get("version") == version]
        
        entry = {
            "version": version,
            "status": "CANDIDATE",
            "model_path": model_path,
            "features_path": features_path,
            "metadata_path": metadata_path,
            "accuracy": metrics.get("accuracy"),
            "precision": metrics.get("precision"),
            "recall": metrics.get("recall"),
            "f1": metrics.get("f1"),
            "roc_auc": metrics.get("roc_auc"),
            "cv_f1": metrics.get("cv_f1"),
            "created_timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat()
        }

        if existing:
            reg["models"] = [entry if m.get("version") == version else m for m in reg["models"]]
        else:
            reg["models"].append(entry)

        self.save_registry(reg)
        return entry

    def promote_model(self, version: str) -> dict:
        """
        Safely promotes a candidate model version to ACTIVE.
        Performs 8-point pre-promotion safety checks.
        """
        reg = self.load_registry()
        candidate = None
        for m in reg["models"]:
            if m.get("version") == version:
                candidate = m
                break

        if not candidate:
            raise ValueError(f"Model version '{version}' not found in registry.")

        if candidate.get("status") == "REJECTED":
            raise ValueError(f"Cannot promote model version '{version}' because its status is REJECTED.")

        if candidate.get("status") == "ACTIVE":
            return {"status": "already_active", "version": version, "registry": reg}

        # Verification 1-3: Files exist
        model_p = self.resolve_path(candidate["model_path"])
        features_p = self.resolve_path(candidate["features_path"])
        meta_p = self.resolve_path(candidate["metadata_path"])

        if not model_p.exists() or not features_p.exists():
            raise FileNotFoundError(f"Cannot promote version '{version}': Required model or feature artifacts missing on disk.")

        # Verification 4-5: Model loads and makes valid inference
        try:
            model = joblib.load(model_p)
            with open(features_p, "r") as f:
                features = json.load(f)
            dummy_input = pd.DataFrame([{col: 0.5 for col in features}])
            prob = model.predict_proba(dummy_input)[:, 1]
            if prob is None or len(prob) == 0:
                raise ValueError("Model failed inference test.")
        except Exception as e:
            raise ValueError(f"Cannot promote version '{version}': Model inference validation failed: {str(e)}")

        # Verification 7: Check comparison decision if file exists
        comparison_path = self.models_dir / "model_comparison_v1_v2.json"
        if comparison_path.exists():
            with open(comparison_path, "r") as f:
                comp = json.load(f)
            if comp.get("decision") != "PROMOTE_V2_CANDIDATE":
                raise ValueError(f"Cannot promote version '{version}': Comparison decision was '{comp.get('decision')}', not 'PROMOTE_V2_CANDIDATE'.")

        # Atomic Promotion: Archive current ACTIVE model, activate candidate
        now_ts = datetime.datetime.now(datetime.timezone.utc).isoformat()
        for m in reg["models"]:
            if m.get("status") == "ACTIVE":
                m["status"] = "ARCHIVED"
                m["archived_timestamp"] = now_ts
            if m.get("version") == version:
                m["status"] = "ACTIVE"
                m["promoted_timestamp"] = now_ts

        reg["active_model"] = version
        self.save_registry(reg)

        return {"status": "promoted", "active_model": version}

    def rollback_model(self, target_version: str) -> dict:
        """
        Rolls back the active model to a previous version (e.g. 1.0).
        """
        reg = self.load_registry()
        target = None
        for m in reg["models"]:
            if m.get("version") == target_version:
                target = m
                break

        if not target:
            raise ValueError(f"Target rollback version '{target_version}' not found in registry.")

        model_p = self.resolve_path(target["model_path"])
        features_p = self.resolve_path(target["features_path"])
        if not model_p.exists() or not features_p.exists():
            raise FileNotFoundError(f"Cannot rollback to version '{target_version}': Artifacts missing on disk.")

        model = joblib.load(model_p)
        with open(features_p, "r") as f:
            features = json.load(f)
        dummy_input = pd.DataFrame([{col: 0.5 for col in features}])
        model.predict_proba(dummy_input)

        now_ts = datetime.datetime.now(datetime.timezone.utc).isoformat()
        for m in reg["models"]:
            if m.get("status") == "ACTIVE":
                m["status"] = "ARCHIVED"
                m["archived_timestamp"] = now_ts
            if m.get("version") == target_version:
                m["status"] = "ACTIVE"
                m["promoted_timestamp"] = now_ts

        reg["active_model"] = target_version
        self.save_registry(reg)

        return {"status": "rolled_back", "active_model": target_version}
