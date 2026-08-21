package com.learningpath.learningpath.dto;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.LearningPathNodeStatus;
import com.learningpath.entity.enums.LearningPathNodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathNodeDto {
    private UUID id;
    private LearningPathNodeType nodeType;
    private String title;
    private String description;
    private String skillName;
    private UUID courseId;
    private String courseTitle;
    private LearningPathNodeStatus status;
    private CourseDifficulty difficulty;
    private Integer estimatedMinutes;
    private Double masteryRequirement;
    private Double currentMastery;
    private Double recommendationScore;
    private Integer order;
    private Integer phaseNumber;
    private String phaseTitle;
    private String actionUrl;
    private String reason;
    private String unlockReason;
    private List<String> prerequisites;
    private Boolean completed;
    private Instant completedAt;
}
