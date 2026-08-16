package com.learningpath.recommendation.engine;

import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.UserSkill;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.SkillGapItemResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

public class SkillGapEngine {

    public static int getProficiencyRank(ProficiencyLevel level) {
        if (level == null) return 0;
        return switch (level) {
            case NOVICE -> 1;
            case BEGINNER -> 2;
            case INTERMEDIATE -> 3;
            case ADVANCED -> 4;
            case EXPERT -> 5;
        };
    }

    public static double getPriorityWeight(SkillPriority priority) {
        if (priority == null) return 1.0;
        return switch (priority) {
            case CRITICAL -> 4.0;
            case HIGH -> 3.0;
            case MEDIUM -> 2.0;
            case LOW -> 1.0;
        };
    }

    public static double getMandatoryMultiplier(boolean isMandatory) {
        return isMandatory ? 1.5 : 1.0;
    }

    public SkillGapItemResponse evaluateSkillGap(CareerSkill careerSkill, UserSkill userSkill) {
        ProficiencyLevel requiredProficiency = careerSkill.getRequiredProficiency();
        ProficiencyLevel userProficiency = userSkill != null ? userSkill.getProficiencyLevel() : null;

        int requiredRank = getProficiencyRank(requiredProficiency);
        int userRank = getProficiencyRank(userProficiency);
        int diff = Math.max(0, requiredRank - userRank);

        GapType gapType;
        if (userRank >= requiredRank) {
            gapType = GapType.NO_GAP;
        } else if (userRank > 0) {
            gapType = GapType.PARTIAL_GAP;
        } else {
            gapType = GapType.FULL_GAP;
        }

        SkillPriority priority = careerSkill.getPriority() != null ? careerSkill.getPriority() : SkillPriority.HIGH;
        boolean mandatory = careerSkill.isMandatory();

        double priorityWeight = getPriorityWeight(priority);
        double mandatoryMultiplier = getMandatoryMultiplier(mandatory);
        double weightedImpact = diff * priorityWeight * mandatoryMultiplier;

        GapSeverity severity;
        if (gapType == GapType.NO_GAP) {
            severity = GapSeverity.LOW;
        } else if (weightedImpact >= 4.5) {
            severity = GapSeverity.CRITICAL;
        } else if (weightedImpact >= 3.0) {
            severity = GapSeverity.HIGH;
        } else if (weightedImpact >= 1.5) {
            severity = GapSeverity.MEDIUM;
        } else {
            severity = GapSeverity.LOW;
        }

        String currentProficiencyStr = userProficiency != null ? userProficiency.name() : "NONE";
        String explanation = generateExplanation(
                careerSkill.getSkill().getName(),
                currentProficiencyStr,
                requiredProficiency.name(),
                gapType,
                priority,
                mandatory
        );

        return new SkillGapItemResponse(
                careerSkill.getSkill().getId(),
                careerSkill.getSkill().getName(),
                careerSkill.getSkill().getCategory(),
                currentProficiencyStr,
                requiredProficiency,
                gapType,
                severity,
                priority,
                mandatory,
                explanation
        );
    }

    public String generateExplanation(
            String skillName,
            String currentProficiency,
            String requiredProficiency,
            GapType gapType,
            SkillPriority priority,
            boolean mandatory
    ) {
        if (gapType == GapType.FULL_GAP) {
            String importanceStr = priority != null ? priority.name().toLowerCase() : "required";
            return String.format("%s is a %s required skill for this career and is currently missing from the learner profile.",
                    skillName, importanceStr);
        } else if (gapType == GapType.PARTIAL_GAP) {
            return String.format("The learner has %s at %s level but the selected career requires %s proficiency.",
                    skillName, capitalize(currentProficiency), capitalize(requiredProficiency));
        } else {
            return String.format("The learner meets or exceeds the required %s proficiency for %s.",
                    capitalize(requiredProficiency), skillName);
        }
    }

    public double calculateOverallGapScore(Iterable<CareerSkill> careerSkills, Map<UUID, UserSkill> userSkillMap) {
        double totalPossibleImpact = 0.0;
        double totalActualGapImpact = 0.0;

        for (CareerSkill cs : careerSkills) {
            int requiredRank = getProficiencyRank(cs.getRequiredProficiency());
            UserSkill us = userSkillMap.get(cs.getSkill().getId());
            int userRank = getProficiencyRank(us != null ? us.getProficiencyLevel() : null);

            double priorityWeight = getPriorityWeight(cs.getPriority());
            double mandatoryMultiplier = getMandatoryMultiplier(cs.isMandatory());

            double maxImpact = requiredRank * priorityWeight * mandatoryMultiplier;
            int diff = Math.max(0, requiredRank - userRank);
            double gapImpact = diff * priorityWeight * mandatoryMultiplier;

            totalPossibleImpact += maxImpact;
            totalActualGapImpact += gapImpact;
        }

        if (totalPossibleImpact == 0.0) {
            return 0.0;
        }

        double score = (totalActualGapImpact / totalPossibleImpact) * 100.0;
        return BigDecimal.valueOf(score)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
