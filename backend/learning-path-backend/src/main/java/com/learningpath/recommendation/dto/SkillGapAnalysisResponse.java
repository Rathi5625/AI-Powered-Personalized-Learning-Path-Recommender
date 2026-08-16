package com.learningpath.recommendation.dto;

import java.util.List;
import java.util.UUID;

public record SkillGapAnalysisResponse(
        UUID userId,
        String userName,
        UUID careerId,
        String careerName,
        int totalRequiredSkills,
        int skillsWithNoGap,
        int partialGaps,
        int fullGaps,
        double overallGapScore,
        List<SkillGapItemResponse> gaps
) {
}
