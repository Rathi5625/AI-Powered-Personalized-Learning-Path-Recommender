package com.learningpath.dto;

import com.learningpath.entity.enums.ProjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateUserProjectRequest(
        ProjectStatus status,

        @Min(0) @Max(100)
        Integer progressPercentage,

        Integer completedMilestones,

        String submissionUrl,

        String notes
) {
}
