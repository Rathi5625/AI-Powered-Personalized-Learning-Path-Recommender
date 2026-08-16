package com.learningpath.recommendation.engine;

import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.SkillGapItemResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationScoringEngine {

    // Weights for 6-factor hybrid scoring model (Total = 1.0 / 100%)
    private static final double WEIGHT_GAP_MATCH = 0.35;
    private static final double WEIGHT_CAREER_PRIORITY = 0.20;
    private static final double WEIGHT_SKILL_COVERAGE = 0.15;
    private static final double WEIGHT_DIFFICULTY_MATCH = 0.10;
    private static final double WEIGHT_COURSE_QUALITY = 0.10;
    private static final double WEIGHT_USER_PREFERENCE = 0.10;

    public CourseRecommendationResponse scoreAndBuildRecommendation(
            int rank,
            Course course,
            List<CourseSkill> courseSkills,
            Map<String, SkillGapItemResponse> gapMap,
            User user,
            Double mlScore
    ) {
        List<String> matchedSkills = new ArrayList<>();
        List<String> gapSkillsAddressed = new ArrayList<>();

        double courseGapScore = 0.0;
        double maxPossibleGapScore = 0.0;
        double highestPriorityWeight = 1.0;
        boolean teachesCriticalSkill = false;

        // Calculate max possible gap score across user's gaps
        for (SkillGapItemResponse gap : gapMap.values()) {
            if (gap.gapType() != GapType.NO_GAP) {
                double priorityW = getPriorityWeight(gap.priority());
                double mandatoryM = gap.mandatory() ? 1.5 : 1.0;
                maxPossibleGapScore += 1.0 * priorityW * mandatoryM;
            }
        }
        if (maxPossibleGapScore == 0.0) maxPossibleGapScore = 1.0;

        // Evaluate course skills
        for (CourseSkill cs : courseSkills) {
            String skillName = cs.getSkill().getName();
            matchedSkills.add(skillName);

            SkillGapItemResponse gap = gapMap.get(skillName);
            if (gap != null && gap.gapType() != GapType.NO_GAP) {
                gapSkillsAddressed.add(skillName);

                double gapWeight = gap.gapType() == GapType.FULL_GAP ? 1.0 : 0.7;
                double priorityW = getPriorityWeight(gap.priority());
                double mandatoryM = gap.mandatory() ? 1.5 : 1.0;

                courseGapScore += gapWeight * priorityW * mandatoryM;

                if (priorityW > highestPriorityWeight) {
                    highestPriorityWeight = priorityW;
                }
                if (gap.priority() == SkillPriority.CRITICAL) {
                    teachesCriticalSkill = true;
                }
            }
        }

        // 1. Skill Gap Match Score (0 - 100)
        double scoreGapMatch = Math.min(100.0, (courseGapScore / maxPossibleGapScore) * 100.0);

        // 2. Career Priority Score (0 - 100)
        double scoreCareerPriority = (highestPriorityWeight / 4.0) * 100.0;

        // 3. Skill Coverage Score (0 - 100)
        long totalGapsCount = gapMap.values().stream().filter(g -> g.gapType() != GapType.NO_GAP).count();
        double coverageRatio = totalGapsCount > 0 ? (double) gapSkillsAddressed.size() / totalGapsCount : 0.0;
        double scoreSkillCoverage = Math.min(100.0, coverageRatio * 100.0);

        // 4. Difficulty Match Score (0 - 100)
        double scoreDifficultyMatch = calculateDifficultyMatch(user.getExperienceLevel(), course.getDifficulty());

        // 5. Course Quality Score (0 - 100)
        double scoreCourseQuality = calculateCourseQuality(course.getRating());

        // 6. User Preference Score (0 - 100)
        double scoreUserPreference = calculateUserPreference(user, course);

        // Weighted Total Deterministic Rule-Based Score
        double rawRuleScore = (scoreGapMatch * WEIGHT_GAP_MATCH)
                + (scoreCareerPriority * WEIGHT_CAREER_PRIORITY)
                + (scoreSkillCoverage * WEIGHT_SKILL_COVERAGE)
                + (scoreDifficultyMatch * WEIGHT_DIFFICULTY_MATCH)
                + (scoreCourseQuality * WEIGHT_COURSE_QUALITY)
                + (scoreUserPreference * WEIGHT_USER_PREFERENCE);

        double ruleBasedScore = BigDecimal.valueOf(rawRuleScore)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        // Calculate combined Final Score (60% Rule + 40% ML if present)
        double rawFinalScore;
        if (mlScore != null) {
            rawFinalScore = (ruleBasedScore * 0.60) + (mlScore * 0.40);
        } else {
            rawFinalScore = ruleBasedScore;
        }

        double finalScore = BigDecimal.valueOf(rawFinalScore)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();

        String explanation = generateExplanation(
                course.getTitle(),
                gapSkillsAddressed,
                teachesCriticalSkill,
                course.getDifficulty(),
                mlScore
        );

        return new CourseRecommendationResponse(
                rank,
                course.getId(),
                course.getTitle(),
                course.getProvider(),
                course.getUrl(),
                course.getDifficulty(),
                course.getCourseType(),
                course.getRating(),
                course.getPrice(),
                course.isFree(),
                ruleBasedScore,
                mlScore,
                finalScore,
                matchedSkills,
                gapSkillsAddressed,
                explanation
        );
    }

    public MlPredictionRequest buildMlPredictionRequest(
            Course course,
            List<CourseSkill> courseSkills,
            Map<String, SkillGapItemResponse> gapMap,
            User user
    ) {
        double courseGapScore = 0.0;
        double maxPossibleGapScore = 0.0;
        double highestPriorityWeight = 1.0;

        List<String> gapSkillsAddressed = new ArrayList<>();

        for (SkillGapItemResponse gap : gapMap.values()) {
            if (gap.gapType() != GapType.NO_GAP) {
                double priorityW = getPriorityWeight(gap.priority());
                double mandatoryM = gap.mandatory() ? 1.5 : 1.0;
                maxPossibleGapScore += 1.0 * priorityW * mandatoryM;
            }
        }
        if (maxPossibleGapScore == 0.0) maxPossibleGapScore = 1.0;

        for (CourseSkill cs : courseSkills) {
            String skillName = cs.getSkill().getName();
            SkillGapItemResponse gap = gapMap.get(skillName);
            if (gap != null && gap.gapType() != GapType.NO_GAP) {
                gapSkillsAddressed.add(skillName);
                double gapWeight = gap.gapType() == GapType.FULL_GAP ? 1.0 : 0.7;
                double priorityW = getPriorityWeight(gap.priority());
                double mandatoryM = gap.mandatory() ? 1.5 : 1.0;

                courseGapScore += gapWeight * priorityW * mandatoryM;
                if (priorityW > highestPriorityWeight) {
                    highestPriorityWeight = priorityW;
                }
            }
        }

        double skillGapScore = Math.min(1.0, courseGapScore / maxPossibleGapScore);
        double careerPriorityScore = highestPriorityWeight / 4.0;

        long totalGapsCount = gapMap.values().stream().filter(g -> g.gapType() != GapType.NO_GAP).count();
        double skillCoverage = totalGapsCount > 0 ? Math.min(1.0, (double) gapSkillsAddressed.size() / totalGapsCount) : 0.0;

        double difficultyMatch = calculateDifficultyMatch(user.getExperienceLevel(), course.getDifficulty()) / 100.0;
        double courseRatingNorm = calculateCourseQuality(course.getRating()) / 100.0;
        double preferenceMatch = calculateUserPreference(user, course) / 100.0;

        long mandatoryGapsCount = gapMap.values().stream().filter(g -> g.gapType() != GapType.NO_GAP && g.mandatory()).count();
        long mandatoryAddressedCount = gapSkillsAddressed.stream().map(gapMap::get).filter(g -> g != null && g.mandatory()).count();
        double mandatorySkillMatch = mandatoryGapsCount > 0 ? (double) mandatoryAddressedCount / mandatoryGapsCount : 1.0;

        double proficiencyGap = gapSkillsAddressed.isEmpty() ? 0.0 : 0.7; // default average proficiency gap
        double courseDurationMatch = (course.getDurationHours() != null && course.getDurationHours() >= 15 && course.getDurationHours() <= 40) ? 1.0 : 0.8;
        double courseQualityScore = 0.7 * courseRatingNorm + 0.3 * courseDurationMatch;

        return MlPredictionRequest.builder()
                .skillGapScore(clamp(skillGapScore))
                .careerPriorityScore(clamp(careerPriorityScore))
                .skillCoverage(clamp(skillCoverage))
                .proficiencyGap(clamp(proficiencyGap))
                .difficultyMatch(clamp(difficultyMatch))
                .courseRating(clamp(courseRatingNorm))
                .preferenceMatch(clamp(preferenceMatch))
                .mandatorySkillMatch(clamp(mandatorySkillMatch))
                .courseDurationMatch(clamp(courseDurationMatch))
                .courseQualityScore(clamp(courseQualityScore))
                .build();
    }

    private double clamp(double val) {
        return Math.max(0.0, Math.min(1.0, val));
    }

    private double getPriorityWeight(SkillPriority priority) {
        if (priority == null) return 1.0;
        return switch (priority) {
            case CRITICAL -> 4.0;
            case HIGH -> 3.0;
            case MEDIUM -> 2.0;
            case LOW -> 1.0;
        };
    }

    public double calculateDifficultyMatch(ExperienceLevel experienceLevel, CourseDifficulty difficulty) {
        if (difficulty == CourseDifficulty.ALL_LEVELS || experienceLevel == null) {
            return 100.0;
        }

        int expRank = switch (experienceLevel) {
            case BEGINNER -> 1;
            case INTERMEDIATE -> 2;
            case ADVANCED -> 3;
            case EXPERT -> 4;
        };

        int diffRank = switch (difficulty) {
            case BEGINNER -> 1;
            case INTERMEDIATE -> 2;
            case ADVANCED -> 3;
            case ALL_LEVELS -> 1;
        };

        int diff = Math.abs(expRank - diffRank);
        return switch (diff) {
            case 0 -> 100.0;
            case 1 -> 75.0;
            case 2 -> 40.0;
            default -> 20.0;
        };
    }

    public double calculateCourseQuality(BigDecimal rating) {
        if (rating == null) return 80.0;
        double r = rating.doubleValue();
        return Math.min(100.0, (r / 5.0) * 100.0);
    }

    public double calculateUserPreference(User user, Course course) {
        double score = 50.0; // Baseline preference score

        if (user.getPreferredContentType() != null && course.getCourseType() != null) {
            PreferredContentType pref = user.getPreferredContentType();
            CourseType type = course.getCourseType();
            if ((pref == PreferredContentType.VIDEO && type == CourseType.VIDEO_COURSE) ||
                (pref == PreferredContentType.INTERACTIVE_EXERCISE && type == CourseType.INTERACTIVE_COURSE) ||
                (pref == PreferredContentType.ARTICLE && type == CourseType.TEXT_TUTORIAL) ||
                (pref == PreferredContentType.PROJECT && type == CourseType.PROJECT_BASED)) {
                score += 30.0;
            }
        }

        if (course.isFree()) {
            score += 20.0;
        }

        return Math.min(100.0, score);
    }

    private String generateExplanation(
            String courseTitle,
            List<String> gapSkillsAddressed,
            boolean teachesCriticalSkill,
            CourseDifficulty difficulty,
            Double mlScore
    ) {
        String baseMsg;
        if (gapSkillsAddressed.isEmpty()) {
            baseMsg = String.format("Provides general learning value for %s level developers.", capitalize(difficulty.name()));
        } else {
            String skillsJoined = String.join(" & ", gapSkillsAddressed);
            if (teachesCriticalSkill && gapSkillsAddressed.size() > 1) {
                baseMsg = String.format("Strongly recommended as it directly addresses your critical skill gaps (%s) required for your target career.", skillsJoined);
            } else if (teachesCriticalSkill) {
                baseMsg = String.format("Teaches critical missing skill %s required for your target career.", skillsJoined);
            } else if (gapSkillsAddressed.size() > 1) {
                baseMsg = String.format("Covers multiple skill gaps (%s) tailored to your learning track.", skillsJoined);
            } else {
                baseMsg = String.format("Addresses your skill gap in %s at %s level.", skillsJoined, capitalize(difficulty.name()));
            }
        }

        if (mlScore != null) {
            return baseMsg + " (ML Model Recommendation Confidence: " + String.format("%.1f%%", mlScore) + ").";
        }
        return baseMsg;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
