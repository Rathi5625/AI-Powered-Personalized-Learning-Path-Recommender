package com.learningpath.dto;

public record SkillGapSummaryResponse(
        int totalRequiredSkills,
        int acquiredSkills,
        int partialGapSkills,
        int missingSkillsCount,
        double matchPercentage
) {}
