package com.learningpath.dto;

import com.learningpath.entity.enums.SkillDifficulty;
import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank(message = "Skill name must not be blank")
        String name,

        @NotBlank(message = "Category must not be blank")
        String category,

        String description,

        SkillDifficulty difficulty
) {
}
