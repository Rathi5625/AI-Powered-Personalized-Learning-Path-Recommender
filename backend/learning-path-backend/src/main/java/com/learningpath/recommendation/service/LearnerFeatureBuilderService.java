package com.learningpath.recommendation.service;

import com.learningpath.ai.dto.LearnerAiContext;
import com.learningpath.entity.Course;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LearnerFeatureBuilderService {

    public MlPredictionRequest buildFeatureVector(LearnerAiContext context, Course course) {
        if (context == null || course == null) {
            return defaultFeatureVector();
        }

        // 1. Existing learner skills lowercase set
        Set<String> learnerSkills = context.getSkills() != null
                ? context.getSkills().stream().map(s -> s.getSkillName().toLowerCase()).collect(Collectors.toSet())
                : Set.of();

        String courseTitleLower = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
        String courseProviderLower = course.getProvider() != null ? course.getProvider().toLowerCase() : "";

        // 2. Skill gap score: 1.0 if course addresses a topic learner does not have verified
        boolean hasSkill = learnerSkills.stream().anyMatch(courseTitleLower::contains);
        double skillGapScore = hasSkill ? 0.40 : 0.85;

        // 3. Career priority score
        String career = context.getTargetCareer() != null ? context.getTargetCareer().toLowerCase() : "";
        double careerPriorityScore = 0.50;
        if (career.contains("software") || career.contains("engineer") || career.contains("developer")) {
            if (courseTitleLower.contains("java") || courseTitleLower.contains("data") || courseTitleLower.contains("web") || courseTitleLower.contains("algorithm")) {
                careerPriorityScore = 0.95;
            }
        }

        // 4. Skill coverage
        double skillCoverage = hasSkill ? 0.50 : 0.80;

        // 5. Proficiency gap
        double proficiencyGap = hasSkill ? 0.30 : 0.70;

        // 6. Difficulty match
        String exp = context.getExperienceLevel() != null ? context.getExperienceLevel() : "BEGINNER";
        String diff = course.getDifficulty() != null ? course.getDifficulty().name() : "INTERMEDIATE";
        double difficultyMatch = 0.70;
        if (exp.equalsIgnoreCase(diff)) {
            difficultyMatch = 0.95;
        } else if (exp.equals("BEGINNER") && diff.equals("INTERMEDIATE")) {
            difficultyMatch = 0.80;
        } else if (exp.equals("BEGINNER") && diff.equals("ADVANCED")) {
            difficultyMatch = 0.40;
        } else if (exp.equals("ADVANCED") && diff.equals("BEGINNER")) {
            difficultyMatch = 0.50;
        }

        // 7. Course rating (0.0 - 1.0)
        double courseRating = course.getRating() != null ? Math.min(1.0, course.getRating().doubleValue() / 5.0) : 0.96;

        // 8. Preference match
        double preferenceMatch = 0.80;

        // 9. Mandatory skill match
        double mandatorySkillMatch = (courseTitleLower.contains("data structure") || courseTitleLower.contains("algorithm") || courseTitleLower.contains("java") || courseTitleLower.contains("system")) ? 0.95 : 0.70;

        // 10. Course duration match
        int weeklyHours = context.getWeeklyCommitmentHours() != null ? context.getWeeklyCommitmentHours() : 10;
        int durationMinutes = course.getDurationMinutes() != null ? course.getDurationMinutes() : 480;
        double courseDurationMatch = (durationMinutes <= weeklyHours * 60 * 2) ? 0.90 : 0.65;

        // 11. Course quality score
        double courseQualityScore = courseRating * 0.95;

        return MlPredictionRequest.builder()
                .skillGapScore(clamp(skillGapScore))
                .careerPriorityScore(clamp(careerPriorityScore))
                .skillCoverage(clamp(skillCoverage))
                .proficiencyGap(clamp(proficiencyGap))
                .difficultyMatch(clamp(difficultyMatch))
                .courseRating(clamp(courseRating))
                .preferenceMatch(clamp(preferenceMatch))
                .mandatorySkillMatch(clamp(mandatorySkillMatch))
                .courseDurationMatch(clamp(courseDurationMatch))
                .courseQualityScore(clamp(courseQualityScore))
                .build();
    }

    private double clamp(double val) {
        return Math.max(0.0, Math.min(1.0, Math.round(val * 100.0) / 100.0));
    }

    private MlPredictionRequest defaultFeatureVector() {
        return MlPredictionRequest.builder()
                .skillGapScore(0.80)
                .careerPriorityScore(0.90)
                .skillCoverage(0.75)
                .proficiencyGap(0.60)
                .difficultyMatch(0.85)
                .courseRating(0.95)
                .preferenceMatch(0.80)
                .mandatorySkillMatch(0.90)
                .courseDurationMatch(0.85)
                .courseQualityScore(0.90)
                .build();
    }
}
