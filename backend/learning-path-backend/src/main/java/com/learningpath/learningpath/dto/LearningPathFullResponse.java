package com.learningpath.learningpath.dto;

import com.learningpath.entity.enums.LearningPathStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathFullResponse {
    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private String targetCareer;
    private String targetRole;
    private LearningPathStatus status;
    private Integer version;
    private Double overallProgress;
    private Double estimatedTotalHours;
    private Double completedHours;
    private Double qualityScore;
    private Map<String, Double> qualityBreakdown;
    private UUID currentNodeId;
    private List<LearningPathNodeDto> nodes;
    private List<LearningPathMilestoneDto> milestones;
    private List<SkillGapDetailDto> skillGaps;
    private Integer weeklyHours;
    private Instant generatedAt;
    private Instant lastRecalculatedAt;
    private String recalculationReason;
}
