package com.learningpath.skilldependency.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LearningOrderRequest(
        @NotNull(message = "Skills list cannot be null")
        List<String> skills
) {}
