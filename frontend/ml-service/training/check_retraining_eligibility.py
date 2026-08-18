"""
Retraining Eligibility Checking Component (Step 9H-5).

Checks raw interaction log counts against configured threshold and persistent state.
"""

import json
import os
import sys
from pathlib import Path
import pandas as pd


def check_eligibility(
    data_csv_path: str = None,
    config_path: str = None,
    state_path: str = None
) -> dict:
    base_dir = Path(__file__).resolve().parent.parent

    if data_csv_path is None:
        data_csv_path = str(base_dir / "data" / "recommendation_interactions.csv")
    if config_path is None:
        config_path = str(base_dir / "config" / "retraining_config.json")
    if state_path is None:
        state_path = str(base_dir / "models" / "retraining_state.json")

    # Load Config
    min_new_interactions = 500
    if Path(config_path).exists():
        with open(config_path, "r") as f:
            cfg = json.load(f)
            min_new_interactions = cfg.get("min_new_interactions", 500)

    # Load State
    last_count = 0
    if Path(state_path).exists():
        with open(state_path, "r") as f:
            st = json.load(f)
            last_count = st.get("last_training_interaction_count", 0)

    # Determine Current Interactions Count
    current_count = 0
    data_file = Path(data_csv_path)
    if data_file.exists():
        try:
            df = pd.read_csv(data_file)
            current_count = len(df)
        except Exception:
            current_count = 0

    new_interactions = max(0, current_count - last_count)
    eligible = new_interactions >= min_new_interactions

    return {
        "eligible": eligible,
        "current_interactions": current_count,
        "previous_interactions": last_count,
        "new_interactions": new_interactions,
        "threshold": min_new_interactions
    }


if __name__ == "__main__":
    res = check_eligibility()
    print("=" * 60)
    print("RETRAINING ELIGIBILITY REPORT")
    print("=" * 60)
    print(f"Current Raw Interactions  : {res['current_interactions']}")
    print(f"Previous Training Count   : {res['previous_interactions']}")
    print(f"New Interactions          : {res['new_interactions']}")
    print(f"Required Threshold        : {res['threshold']}")
    print(f"Eligibility Status        : {'ELIGIBLE' if res['eligible'] else 'NOT ELIGIBLE'}")
    print("=" * 60)
