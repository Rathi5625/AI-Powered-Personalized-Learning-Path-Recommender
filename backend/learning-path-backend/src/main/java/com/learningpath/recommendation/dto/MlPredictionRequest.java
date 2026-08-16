package com.learningpath.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MlPredictionRequest(
        @JsonProperty("skill_gap_score") double skillGapScore,
        @JsonProperty("career_priority_score") double careerPriorityScore,
        @JsonProperty("skill_coverage") double skillCoverage,
        @JsonProperty("proficiency_gap") double proficiencyGap,
        @JsonProperty("difficulty_match") double difficultyMatch,
        @JsonProperty("course_rating") double courseRating,
        @JsonProperty("preference_match") double preferenceMatch,
        @JsonProperty("mandatory_skill_match") double mandatorySkillMatch,
        @JsonProperty("course_duration_match") double courseDurationMatch,
        @JsonProperty("course_quality_score") double courseQualityScore
) {
}
