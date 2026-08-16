package com.learningpath.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AiRequest(
        @NotNull(message = "AI operation is required")
        AiOperation operation,

        @Valid
        AiContext context
) {}
