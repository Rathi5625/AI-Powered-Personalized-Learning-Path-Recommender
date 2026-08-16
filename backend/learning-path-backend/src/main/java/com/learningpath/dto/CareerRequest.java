package com.learningpath.dto;

import jakarta.validation.constraints.NotBlank;

public record CareerRequest(
        @NotBlank(message = "Career name must not be blank")
        String name,

        String description,

        String category
) {
}
