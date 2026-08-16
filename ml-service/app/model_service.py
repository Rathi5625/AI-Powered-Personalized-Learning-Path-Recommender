import os
import json
import joblib
import pandas as pd
from typing import Dict, Any, Optional

from app.schemas import PredictionRequest, PredictionResponse, ModelInfoResponse
from app.model_registry import ModelRegistry

class ModelService:
    def __init__(self):
        self.registry = ModelRegistry()
        self.model = None
        self.feature_columns = None
        self.metadata: Optional[Dict[str, Any]] = None
        self.active_version: str = "1.0"
        self.is_loaded = False

    def load_model(self) -> bool:
        try:
            self.model, self.feature_columns, self.metadata = self.registry.get_active_model_artifacts()
            self.active_version = self.registry.get_active_version()
            self.is_loaded = True
            print(f"[ModelService SUCCESS] Active model version '{self.active_version}' loaded ({len(self.feature_columns)} features).")
            return True
        except Exception as e:
            print(f"[ModelService ERROR] Failed to load active model: {e}")
            self.is_loaded = False
            return False

    def predict(self, request: PredictionRequest) -> PredictionResponse:
        if not self.is_loaded or self.model is None:
            # Try reloading in case registry changed
            if not self.load_model():
                raise RuntimeError("Model is not loaded.")

        raw_data = request.model_dump()
        input_data = {col: [raw_data[col]] for col in self.feature_columns}
        df_input = pd.DataFrame(input_data)

        probabilities = self.model.predict_proba(df_input)
        prob = float(probabilities[0][1])

        score = round(prob * 100.0, 2)
        recommended = prob >= 0.5

        return PredictionResponse(
                recommendation_probability=round(prob, 4),
                recommendation_score=score,
                recommended=recommended,
                model_version=self.active_version
        )

    def get_model_info(self) -> ModelInfoResponse:
        if not self.is_loaded or self.metadata is None:
            self.load_model()
            if not self.is_loaded:
                raise RuntimeError("Model metadata is not available.")

        return ModelInfoResponse(
                model_type=self.metadata.get("model_type", "GradientBoostingClassifier"),
                version=self.active_version,
                features=self.feature_columns or [],
                training_samples=self.metadata.get("training_samples", 0),
                test_samples=self.metadata.get("test_samples", 0),
                accuracy=self.metadata.get("accuracy", 0.0),
                precision=self.metadata.get("precision", 0.0),
                recall=self.metadata.get("recall", 0.0),
                f1_score=self.metadata.get("f1_score", self.metadata.get("f1", 0.0)),
                roc_auc=self.metadata.get("roc_auc", 0.0),
                cv_f1_mean=self.metadata.get("cv_f1_mean", self.metadata.get("cv_f1", 0.0)),
                training_timestamp=self.metadata.get("training_timestamp")
        )

# Global Singleton Instance
model_service = ModelService()
