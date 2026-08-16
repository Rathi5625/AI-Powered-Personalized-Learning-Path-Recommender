package com.learningpath.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MlPredictionResponse(
        @JsonProperty("recommendation_probability") Double recommendationProbability,
        @JsonProperty("recommendation_score") Double recommendationScore,
        @JsonProperty("recommended") Boolean recommended,
        @JsonProperty("model_version") String modelVersion
) {
}
