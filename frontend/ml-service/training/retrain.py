"""
Automated Model Retraining Workflow Pipeline (Step 9H-5).

Orchestrates eligibility checking, concurrency locking, real dataset consolidation,
candidate model training, dual model comparison, safe registry promotion, state update,
and historical logging with dry-run support.
"""

import argparse
import datetime
import json
import logging
import os
import sys
import uuid
from pathlib import Path

# Add ml-service root directory to sys.path
BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger("RetrainingPipeline")

from app.model_registry import ModelRegistry
from training.check_retraining_eligibility import check_eligibility
from training.build_real_dataset import build_real_dataset
from training.train_model_v2 import train_candidate_model_v2
from training.compare_models import compare_models


class RetrainingLock:
    """Context manager for concurrency safety using models/retraining.lock."""

    def __init__(self, lock_path: Path):
        self.lock_path = lock_path

    def __enter__(self):
        if self.lock_path.exists():
            logger.warning("Retraining already in progress. Lock file exists.")
            sys.exit(0)
        self.lock_path.parent.mkdir(parents=True, exist_ok=True)
        self.lock_path.write_text(f"PID={os.getpid()}\nStarted={datetime.datetime.now(datetime.timezone.utc).isoformat()}\n")
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.lock_path.exists():
            try:
                self.lock_path.unlink()
            except Exception as e:
                logger.error(f"Failed to remove lock file: {e}")


def get_next_candidate_version(registry: ModelRegistry) -> str:
    """Calculates next candidate version string (e.g. 1.0 -> 2.0)."""
    active_ver = registry.get_active_version()
    try:
        major = int(active_ver.split(".")[0])
        return f"{major + 1}.0"
    except Exception:
        return "2.0"


def record_history_entry(history_path: Path, entry: dict):
    """Appends a retraining run entry to models/retraining_history.json."""
    history_path.parent.mkdir(parents=True, exist_ok=True)
    history = []
    if history_path.exists():
        try:
            with open(history_path, "r") as f:
                history = json.load(f)
        except Exception:
            history = []

    history.append(entry)
    tmp_path = history_path.with_suffix(".tmp")
    with open(tmp_path, "w") as f:
        json.dump(history, f, indent=2)
    os.replace(tmp_path, history_path)


def run_retraining_pipeline(dry_run: bool = False, force: bool = False, config_path: str = None) -> dict:
    base_dir = Path(__file__).resolve().parent.parent
    models_dir = base_dir / "models"
    lock_file = models_dir / "retraining.lock"
    data_csv = base_dir / "data" / "recommendation_interactions.csv"
    real_dataset_csv = base_dir / "data" / "real_interaction_training_data.csv"
    state_file = models_dir / "retraining_state.json"
    history_file = models_dir / "retraining_history.json"

    registry = ModelRegistry(str(models_dir))
    run_id = str(uuid.uuid4())
    started_at = datetime.datetime.now(datetime.timezone.utc).isoformat()

    logger.info("=" * 60)
    logger.info("STARTING AUTOMATED MODEL RETRAINING WORKFLOW")
    logger.info("=" * 60)

    # Concurrency Lock
    with RetrainingLock(lock_file):

        # 1. Eligibility Check
        eligibility = check_eligibility(
            data_csv_path=str(data_csv),
            config_path=config_path,
            state_path=str(state_file)
        )

        logger.info(f"Current Raw Interactions : {eligibility['current_interactions']}")
        logger.info(f"Previous Training Count  : {eligibility['previous_interactions']}")
        logger.info(f"New Interactions         : {eligibility['new_interactions']}")
        logger.info(f"Required Threshold       : {eligibility['threshold']}")

        if not eligibility["eligible"] and not force:
            logger.info("Retraining not required. New interactions below threshold.")
            history_entry = {
                "run_id": run_id,
                "started_at": started_at,
                "completed_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "new_interactions": eligibility["new_interactions"],
                "status": "SKIPPED",
                "previous_model": registry.get_active_version(),
                "reason": "New interactions below configured threshold."
            }
            if not dry_run:
                record_history_entry(history_file, history_entry)
            return {"status": "SKIPPED", "eligibility": eligibility}

        next_version = get_next_candidate_version(registry)
        active_version = registry.get_active_version()

        # Dry Run Handler
        if dry_run:
            logger.info("=" * 60)
            logger.info("DRY RUN MODE ENABLED")
            logger.info("=" * 60)
            logger.info(f"Eligibility      : ELIGIBLE")
            logger.info(f"Active Version   : {active_version}")
            logger.info(f"Next Candidate   : {next_version}")
            logger.info("DRY RUN SUMMARY: No models, registry, or state files were modified.")
            return {
                "status": "DRY_RUN_ELIGIBLE",
                "active_version": active_version,
                "candidate_version": next_version,
                "eligibility": eligibility
            }

        # 2. Build Real Dataset
        logger.info("Stage 1/5: Building real interaction dataset...")
        df_real, dataset_report = build_real_dataset(
            input_csv=str(data_csv),
            output_csv=str(real_dataset_csv),
            min_samples=100
        )

        if df_real is None:
            logger.warning("Dataset consolidation returned insufficient data.")
            history_entry = {
                "run_id": run_id,
                "started_at": started_at,
                "completed_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "new_interactions": eligibility["new_interactions"],
                "candidate_version": next_version,
                "status": "REJECTED",
                "previous_model": active_version,
                "reason": "Consolidated interaction data was insufficient for training."
            }
            record_history_entry(history_file, history_entry)
            return {"status": "REJECTED", "reason": "Insufficient consolidated dataset"}

        # 3. Train Candidate Model in Versioned Folder
        candidate_version_dir = models_dir / f"v{next_version.split('.')[0]}"
        candidate_version_dir.mkdir(parents=True, exist_ok=True)

        logger.info(f"Stage 2/5: Training Candidate Model v{next_version}...")

        try:
            train_meta = train_candidate_model_v2(
                data_path=str(real_dataset_csv),
                models_dir=str(candidate_version_dir)
            )

            # Register Candidate in ModelRegistry
            registry.register_candidate(
                version=next_version,
                model_path=str(candidate_version_dir / "candidate_recommendation_model_v2.joblib"),
                features_path=str(candidate_version_dir / "candidate_feature_columns_v2.json"),
                metadata_path=str(candidate_version_dir / "candidate_model_v2_metadata.json"),
                metrics=train_meta
            )

            # Also point root candidate path for comparison module compatibility
            import shutil
            shutil.copy(candidate_version_dir / "candidate_recommendation_model_v2.joblib", models_dir / "candidate_recommendation_model_v2.joblib")
            shutil.copy(candidate_version_dir / "candidate_feature_columns_v2.json", models_dir / "candidate_feature_columns_v2.json")
            shutil.copy(candidate_version_dir / "candidate_model_v2_metadata.json", models_dir / "candidate_model_v2_metadata.json")

        except Exception as e:
            logger.error(f"Training failed: {e}")
            history_entry = {
                "run_id": run_id,
                "started_at": started_at,
                "completed_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "new_interactions": eligibility["new_interactions"],
                "candidate_version": next_version,
                "status": "FAILED",
                "previous_model": active_version,
                "reason": f"Model training exception: {str(e)}"
            }
            record_history_entry(history_file, history_entry)
            return {"status": "FAILED", "reason": str(e)}

        # 4. Compare Models
        logger.info("Stage 3/5: Comparing Candidate Model v2 with Active Model v1...")
        comp_report = compare_models(
            models_dir=str(models_dir),
            data_path=str(real_dataset_csv)
        )

        decision = comp_report.get("decision")
        reason = comp_report.get("reason")
        logger.info(f"Comparison Result: Decision={decision}, Reason={reason}")

        # 5. Promotion or Rejection
        if decision == "PROMOTE_V2_CANDIDATE":
            logger.info("Stage 4/5: Promoting Candidate Model to ACTIVE...")
            try:
                registry.promote_model(next_version)
                logger.info(f"Successfully promoted Model v{next_version} to ACTIVE.")

                # Update State File
                new_state = {
                    "last_training_timestamp": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                    "last_training_interaction_count": eligibility["current_interactions"],
                    "last_active_model": next_version,
                    "last_training_status": "SUCCESS"
                }
                state_file.write_text(json.dumps(new_state, indent=2))

                history_entry = {
                    "run_id": run_id,
                    "started_at": started_at,
                    "completed_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                    "new_interactions": eligibility["new_interactions"],
                    "candidate_version": next_version,
                    "status": "PROMOTED",
                    "previous_model": active_version,
                    "new_model": next_version,
                    "reason": reason
                }
                record_history_entry(history_file, history_entry)
                return {"status": "PROMOTED", "active_model": next_version}

            except Exception as e:
                logger.error(f"Promotion failed: {e}")
                history_entry = {
                    "run_id": run_id,
                    "started_at": started_at,
                    "completed_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                    "new_interactions": eligibility["new_interactions"],
                    "candidate_version": next_version,
                    "status": "FAILED",
                    "previous_model": active_version,
                    "reason": f"Promotion failure: {str(e)}"
                }
                record_history_entry(history_file, history_entry)
                return {"status": "FAILED", "reason": str(e)}

        else:
            logger.info(f"Stage 4/5: Candidate v{next_version} was REJECTED. Preserving Active Model v{active_version}.")
            # Mark candidate REJECTED in registry
            reg_data = registry.load_registry()
            for m in reg_data.get("models", []):
                if m.get("version") == next_version:
                    m["status"] = "REJECTED"
            registry.save_registry(reg_data)

            history_entry = {
                "run_id": run_id,
                "started_at": started_at,
                "completed_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "new_interactions": eligibility["new_interactions"],
                "candidate_version": next_version,
                "status": "REJECTED",
                "previous_model": active_version,
                "reason": reason
            }
            record_history_entry(history_file, history_entry)
            return {"status": "REJECTED", "reason": reason}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Automated ML Model Retraining Workflow")
    parser.add_argument("--dry-run", action="store_true", help="Calculate eligibility and output plan without making changes")
    parser.add_argument("--force", action="store_true", help="Force retraining run regardless of interaction threshold")
    parser.add_argument("--config", type=str, default=None, help="Path to custom retraining_config.json")

    args = parser.parse_args()
    run_retraining_pipeline(dry_run=args.dry_run, force=args.force, config_path=args.config)
