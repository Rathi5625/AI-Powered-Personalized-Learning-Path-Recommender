from pydantic import BaseModel, Field
from typing import List, Optional

class PredictionRequest(BaseModel):
    skill_gap_score: float = Field(..., ge=0.0, le=1.0, description="Weighted gap score addressed by course (0.0 to 1.0)")
    career_priority_score: float = Field(..., ge=0.0, le=1.0, description="Average career skill priority weight (0.0 to 1.0)")
    skill_coverage: float = Field(..., ge=0.0, le=1.0, description="Proportion of total learner skill gaps covered (0.0 to 1.0)")
    proficiency_gap: float = Field(..., ge=0.0, le=1.0, description="Average gap between target and current proficiency (0.0 to 1.0)")
    difficulty_match: float = Field(..., ge=0.0, le=1.0, description="Experience level vs course difficulty alignment (0.0 to 1.0)")
    course_rating: float = Field(..., ge=0.0, le=1.0, description="Normalized course rating (0.0 to 1.0)")
    preference_match: float = Field(..., ge=0.0, le=1.0, description="Preference match for content type and price (0.0 to 1.0)")
    mandatory_skill_match: float = Field(..., ge=0.0, le=1.0, description="Proportion of mandatory missing skills covered (0.0 to 1.0)")
    course_duration_match: float = Field(..., ge=0.0, le=1.0, description="Course duration appropriateness score (0.0 to 1.0)")
    course_quality_score: float = Field(..., ge=0.0, le=1.0, description="Composite course quality score (0.0 to 1.0)")

    model_config = {
        "json_schema_extra": {
            "example": {
                "skill_gap_score": 0.90,
                "career_priority_score": 1.00,
                "skill_coverage": 0.80,
                "proficiency_gap": 0.70,
                "difficulty_match": 0.90,
                "course_rating": 0.90,
                "preference_match": 0.80,
                "mandatory_skill_match": 1.00,
                "course_duration_match": 0.90,
                "course_quality_score": 0.90
            }
        }
    }

class PredictionResponse(BaseModel):
    recommendation_probability: float = Field(..., description="Predicted recommendation probability (0.0 to 1.0)")
    recommendation_score: float = Field(..., description="Percentage recommendation score (0.0 to 100.0)")
    recommended: bool = Field(..., description="True if probability >= 0.5")
    model_version: str = Field(default="1.0", description="Model version identifier")

class ModelInfoResponse(BaseModel):
    model_type: str
    version: str = "1.0"
    features: List[str]
    training_samples: int
    test_samples: int
    accuracy: float
    precision: float
    recall: float
    f1_score: float
    roc_auc: float
    cv_f1_mean: float
    training_timestamp: Optional[str] = None
