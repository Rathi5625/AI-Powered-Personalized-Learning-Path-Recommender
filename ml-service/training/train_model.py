import os
import json
from datetime import datetime, timezone
import numpy as np
import pandas as pd
import joblib

from sklearn.ensemble import GradientBoostingClassifier
from sklearn.model_selection import train_test_split, StratifiedKFold, cross_val_score
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    roc_auc_score,
    confusion_matrix,
    classification_report
)

RANDOM_STATE = 42

def train_and_evaluate_model():
    print("=" * 60)
    print("STEP 9C: TRAINING & EVALUATING RECOMMENDATION ML MODEL")
    print("=" * 60)

    # 1. Load Dataset
    data_path = os.path.join(os.path.dirname(__file__), "..", "data", "training_data.csv")
    if not os.path.exists(data_path):
        raise FileNotFoundError(f"Training dataset not found at: {data_path}. Please run generate_dataset.py first.")

    df = pd.read_csv(data_path)
    print(f"Loaded dataset from: {os.path.abspath(data_path)}")
    print(f"Dataset Shape: {df.shape}")

    # Validate dataset
    target_col = "recommendation_label"
    if target_col not in df.columns:
        raise ValueError(f"Target column '{target_col}' not found in dataset.")

    X = df.drop(columns=[target_col])
    y = df[target_col]
    feature_names = list(X.columns)

    print(f"Features ({len(feature_names)}): {feature_names}\n")

    # 2. 80/20 Stratified Train/Test Split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=RANDOM_STATE, stratify=y
    )

    print(f"Training Samples: {len(X_train)}")
    print(f"Testing Samples:  {len(X_test)}\n")

    # 3. Model Training (GradientBoostingClassifier)
    print("Training GradientBoostingClassifier(random_state=42)...")
    clf = GradientBoostingClassifier(
        n_estimators=100,
        learning_rate=0.1,
        max_depth=4,
        random_state=RANDOM_STATE
    )
    clf.fit(X_train, y_train)

    # 4. Predictions & Probabilities
    y_pred = clf.predict(X_test)
    y_proba = clf.predict_proba(X_test)[:, 1]

    # 5. Evaluate Metrics
    acc = accuracy_score(y_test, y_pred)
    prec = precision_score(y_test, y_pred)
    rec = recall_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    roc_auc = roc_auc_score(y_test, y_proba)
    cm = confusion_matrix(y_test, y_pred)

    # 6. 5-Fold Stratified Cross-Validation
    print("Running 5-Fold Stratified Cross-Validation...")
    skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)
    cv_scores = cross_val_score(clf, X, y, cv=skf, scoring='f1')

    # 7. Model Evaluation Report Output
    print("\n" + "=" * 60)
    print("MODEL EVALUATION METRICS (TEST SET)")
    print("=" * 60)
    print(f"Accuracy:        {acc:.4f}")
    print(f"Precision:       {prec:.4f}")
    print(f"Recall:          {rec:.4f}")
    print(f"F1 Score:        {f1:.4f}")
    print(f"ROC-AUC:         {roc_auc:.4f}")
    print(f"5-Fold CV F1:    {cv_scores.mean():.4f} (+/- {cv_scores.std():.4f})\n")

    print("Confusion Matrix:")
    print(cm)
    print("\nClassification Report:")
    print(classification_report(y_test, y_pred, target_names=["Not Recommended (0)", "Recommended (1)"]))

    # 8. Feature Importance
    importances = clf.feature_importances_
    importance_pairs = sorted(zip(feature_names, importances), key=lambda x: x[1], reverse=True)

    print("\n" + "=" * 60)
    print("FEATURE IMPORTANCE RANKING")
    print("=" * 60)
    for feat, imp in importance_pairs:
        print(f"{feat:<25} {imp:.4f}")
    print("=" * 60 + "\n")

    # 9. Model Artifact Exporting
    models_dir = os.path.join(os.path.dirname(__file__), "..", "models")
    os.makedirs(models_dir, exist_ok=True)

    # Save model binary
    model_path = os.path.join(models_dir, "recommendation_model.joblib")
    joblib.dump(clf, model_path)
    print(f"Saved model binary to: {os.path.abspath(model_path)}")

    # Save feature columns JSON
    features_path = os.path.join(models_dir, "feature_columns.json")
    with open(features_path, "w") as f:
        json.dump(feature_names, f, indent=2)
    print(f"Saved feature columns to: {os.path.abspath(features_path)}")

    # Save model metadata JSON
    metadata = {
        "model_type": "GradientBoostingClassifier",
        "training_samples": len(X_train),
        "test_samples": len(X_test),
        "feature_names": feature_names,
        "random_state": RANDOM_STATE,
        "accuracy": round(acc, 4),
        "precision": round(prec, 4),
        "recall": round(rec, 4),
        "f1_score": round(f1, 4),
        "roc_auc": round(roc_auc, 4),
        "cv_f1_mean": round(cv_scores.mean(), 4),
        "cv_f1_std": round(cv_scores.std(), 4),
        "training_timestamp": datetime.now(timezone.utc).isoformat()
    }

    metadata_path = os.path.join(models_dir, "model_metadata.json")
    with open(metadata_path, "w") as f:
        json.dump(metadata, f, indent=2)
    print(f"Saved model metadata to: {os.path.abspath(metadata_path)}")
    print("\nModel training and evaluation successfully completed!")

if __name__ == "__main__":
    train_and_evaluate_model()
