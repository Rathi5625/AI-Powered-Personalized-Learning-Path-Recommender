package com.learningpath.learningpath.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapDetailDto {
    private String skill;
    private Double requiredLevel;
    private Double currentMastery;
    private Double gap;
    private Double priority;
    private String status;
}
