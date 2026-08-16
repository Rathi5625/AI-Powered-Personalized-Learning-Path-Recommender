package com.learningpath.learningpath.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdaptLearningPathRequest(

        @NotNull(message = "careerId is required for adaptation")
        UUID careerId
) {}
