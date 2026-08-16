package com.learningpath.learningpath.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GenerateLearningPathRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        UUID careerId
) {}
