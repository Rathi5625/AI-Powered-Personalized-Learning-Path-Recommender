package com.learningpath.learningpath.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathMilestoneDto {
    private String id;
    private String title;
    private String description;
    private String targetSkill;
    private Double requiredMastery;
    private Double currentMastery;
    private Boolean completed;
    private Integer targetPhase;
}
