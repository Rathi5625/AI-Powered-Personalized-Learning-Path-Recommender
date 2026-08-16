"""
Real Interaction Training Dataset Construction Pipeline for Recommendation Model v2.

This module loads exported user interaction logs, consolidates user-course journeys,
assigns binary recommendation labels, validates data quality, and outputs
data/real_interaction_training_data.csv when sufficient real data exists.
"""

import os
import sys
import pandas as pd
import numpy as np
from pathlib import Path

# Required 10 ML features matching Model v1 representation
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

POSITIVE_INTERACTIONS = {"STARTED", "COMPLETED", "LIKED"}
NEGATIVE_INTERACTIONS = {"SKIPPED"}
WEAK_POSITIVE_INTERACTIONS = {"CLICKED"}
INSUFFICIENT_SIGNAL_INTERACTIONS = {"VIEWED"}


def consolidate_interactions_group(group: pd.DataFrame) -> pd.Series:
    """
    Consolidates multiple interaction events for a single (user_id, course_id) pair into a single label.

    Consolidation Logic:
    - STARTED, COMPLETED, LIKED -> Strong Positive (label = 1)
    - SKIPPED -> Negative (label = 0)
    - CLICKED -> Weak Positive (label = 1)
    - VIEWED only -> Insufficient signal (label = NaN, to be filtered out)
    """
    types = set(group["interaction_type"].str.upper())

    # Check for strong positive signals first
    if types.intersection(POSITIVE_INTERACTIONS):
        label = 1
    # Check for negative signal next
    elif types.intersection(NEGATIVE_INTERACTIONS):
        label = 0
    # Check for weak positive signal
    elif types.intersection(WEAK_POSITIVE_INTERACTIONS):
        label = 1
    else:
        # VIEWED only or unrecognized interaction
        label = np.nan

    # Take the latest feature values from the group
    first_row = group.iloc[-1].copy()
    first_row["recommendation_label"] = label
    return first_row


def consolidate_user_course_interactions(df: pd.DataFrame) -> pd.DataFrame:
    """
    Groups raw interactions by (user_id, course_id) and applies label consolidation.
    """
    if "user_id" not in df.columns or "course_id" not in df.columns or "interaction_type" not in df.columns:
        raise ValueError("Input dataframe must contain 'user_id', 'course_id', and 'interaction_type' columns.")

    # Sort chronologically if created_at is present
    if "created_at" in df.columns:
        df = df.sort_values("created_at")

    grouped = df.groupby(["user_id", "course_id"], as_index=False, group_keys=False).apply(consolidate_interactions_group)
    
    # Filter out rows with NaN label (e.g., VIEWED-only interactions)
    consolidated = grouped.dropna(subset=["recommendation_label"]).copy()
    consolidated["recommendation_label"] = consolidated["recommendation_label"].astype(int)
    
    return consolidated.reset_index(drop=True)


def validate_dataset_quality(df: pd.DataFrame) -> dict:
    """
    Validates feature ranges, missing values, duplicates, and label distribution.
    """
    report = {
        "is_valid": True,
        "errors": [],
        "warnings": [],
        "duplicate_count": 0,
        "missing_values": 0,
        "positive_count": 0,
        "negative_count": 0
    }

    # Check missing features
    missing_cols = [col for col in FEATURE_COLUMNS if col not in df.columns]
    if missing_cols:
        report["is_valid"] = False
        report["errors"].append(f"Missing required feature columns: {missing_cols}")
        return report

    # Check NaNs and Infs
    na_count = df[FEATURE_COLUMNS].isna().sum().sum()
    inf_count = np.isinf(df[FEATURE_COLUMNS].select_dtypes(include=[np.number])).sum().sum()
    if na_count > 0 or inf_count > 0:
        report["is_valid"] = False
        report["errors"].append(f"Dataset contains {na_count} NaN values and {inf_count} Inf values.")

    # Check feature bounds
    for col in FEATURE_COLUMNS:
        if col == "course_rating":
            out_of_bounds = ((df[col] < 0.0) | (df[col] > 5.0)).sum()
            if out_of_bounds > 0:
                report["is_valid"] = False
                report["errors"].append(f"Feature '{col}' has {out_of_bounds} values outside [0.0, 5.0].")
        else:
            out_of_bounds = ((df[col] < 0.0) | (df[col] > 1.0)).sum()
            if out_of_bounds > 0:
                report["is_valid"] = False
                report["errors"].append(f"Feature '{col}' has {out_of_bounds} values outside [0.0, 1.0].")

    # Check duplicates
    duplicates = df.duplicated(subset=["user_id", "course_id"]).sum()
    report["duplicate_count"] = duplicates
    if duplicates > 0:
        report["is_valid"] = False
        report["errors"].append(f"Dataset contains {duplicates} duplicate (user_id, course_id) rows.")

    # Label counts
    labels = df["recommendation_label"].value_counts()
    report["positive_count"] = int(labels.get(1, 0))
    report["negative_count"] = int(labels.get(0, 0))

    if report["positive_count"] == 0 or report["negative_count"] == 0:
        report["is_valid"] = False
        report["errors"].append("Dataset label distribution is single-class. Must contain both 0 and 1 labels.")

    return report


def build_real_dataset(input_csv: str, output_csv: str, min_samples: int = 100) -> tuple:
    """
    Pipeline entry point to construct real interaction dataset.
    """
    input_path = Path(input_csv)
    if not input_path.exists():
        print("Insufficient real interaction data for Model v2.")
        print(f"Reason: Input file '{input_csv}' does not exist.")
        return None, {"status": "file_not_found"}

    df_raw = pd.read_csv(input_path)
    if df_raw.empty:
        print("Insufficient real interaction data for Model v2.")
        print("Reason: Input CSV file is empty.")
        return None, {"status": "empty_file"}

    # Consolidate user-course interactions
    consolidated_df = consolidate_user_course_interactions(df_raw)

    total_consolidated = len(consolidated_df)
    unique_users = consolidated_df["user_id"].nunique() if "user_id" in consolidated_df.columns else 0
    unique_courses = consolidated_df["course_id"].nunique() if "course_id" in consolidated_df.columns else 0

    print("=" * 60)
    print("REAL INTERACTION DATASET CONSOLIDATION REPORT")
    print("=" * 60)
    print(f"Total raw interaction logs      : {len(df_raw)}")
    print(f"Consolidated user-course pairs  : {total_consolidated}")
    print(f"Unique users                    : {unique_users}")
    print(f"Unique courses                  : {unique_courses}")
    print(f"Configured minimum threshold    : {min_samples}")

    if total_consolidated < min_samples:
        print("\nSTATUS: Insufficient real interaction data for Model v2.")
        print(f"Reason: Consolidated pairs ({total_consolidated}) < Minimum threshold ({min_samples}).")
        return None, {
            "status": "insufficient_data",
            "total_consolidated": total_consolidated,
            "min_samples": min_samples
        }

    # Validate Quality
    quality_report = validate_dataset_quality(consolidated_df)
    if not quality_report["is_valid"]:
        print("\nSTATUS: Insufficient real interaction data for Model v2.")
        print(f"Reason: Data quality validation failed. Errors: {quality_report['errors']}")
        return None, {"status": "quality_failed", "report": quality_report}

    # Prepare export dataframe
    export_cols = ["user_id", "course_id"] + FEATURE_COLUMNS + ["recommendation_label"]
    output_df = consolidated_df[export_cols].copy()

    # Save dataset
    output_path = Path(output_csv)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_df.to_csv(output_path, index=False)

    pos_rate = (quality_report["positive_count"] / total_consolidated) * 100

    print("\n" + "=" * 60)
    print("DATASET GENERATION SUCCESSFUL")
    print("=" * 60)
    print(f"Output File     : {output_path}")
    print(f"Dataset Shape   : {output_df.shape}")
    print(f"Positive Count  : {quality_report['positive_count']}")
    print(f"Negative Count  : {quality_report['negative_count']}")
    print(f"Positive Rate   : {pos_rate:.2f}%")
    print("=" * 60)

    return output_df, quality_report


if __name__ == "__main__":
    base_dir = Path(__file__).resolve().parent.parent
    input_file = str(base_dir / "data" / "recommendation_interactions.csv")
    output_file = str(base_dir / "data" / "real_interaction_training_data.csv")

    build_real_dataset(input_file, output_file, min_samples=100)
