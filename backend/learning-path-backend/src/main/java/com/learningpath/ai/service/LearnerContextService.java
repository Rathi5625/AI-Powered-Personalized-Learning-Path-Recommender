package com.learningpath.ai.service;

import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.adaptive.service.LearnerBehaviorService;
import com.learningpath.adaptive.service.LearnerMasteryService;
import com.learningpath.ai.dto.LearnerAiContext;
import com.learningpath.entity.AssessmentResult;
import com.learningpath.entity.User;
import com.learningpath.entity.UserProgress;
import com.learningpath.entity.UserSkill;
import com.learningpath.repository.AssessmentResultRepository;
import com.learningpath.repository.UserProgressRepository;
import com.learningpath.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerContextService {

    private final UserSkillRepository userSkillRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final UserProgressRepository userProgressRepository;
    private final LearnerMasteryService masteryService;
    private final LearnerBehaviorService behaviorService;

    @Transactional(readOnly = true)
    public LearnerAiContext buildContext(User user) {
        if (user == null) {
            return null;
        }

        // 1. Skills
        List<UserSkill> userSkills = userSkillRepository.findByUserId(user.getId());
        List<LearnerAiContext.LearnerSkillInfo> skillInfos = userSkills.stream()
                .map(us -> LearnerAiContext.LearnerSkillInfo.builder()
                        .skillName(us.getSkill() != null ? us.getSkill().getName() : "Unknown")
                        .proficiencyLevel(us.getProficiencyLevel() != null ? us.getProficiencyLevel().name() : "BEGINNER")
                        .verified(us.isVerified())
                        .build())
                .collect(Collectors.toList());

        // 2. Assessments
        List<AssessmentResult> results = assessmentResultRepository.findAllByUserIdOrderByCompletedAtDesc(user.getId());
        List<LearnerAiContext.LearnerAssessmentSummary> assessmentSummaries = results.stream()
                .map(ar -> LearnerAiContext.LearnerAssessmentSummary.builder()
                        .title(ar.getAssessment() != null ? ar.getAssessment().getTitle() : "Skill Assessment")
                        .skillName(ar.getAssessment() != null && ar.getAssessment().getSkill() != null ? ar.getAssessment().getSkill().getName() : "General")
                        .scorePercentage(ar.getScore() != null ? ar.getScore().doubleValue() : 0.0)
                        .timeSpentSeconds(0)
                        .createdAt(ar.getCompletedAt() != null ? ar.getCompletedAt().toString() : "")
                        .build())
                .collect(Collectors.toList());

        // 3. Course Progress
        List<UserProgress> progresses = userProgressRepository.findByUserId(user.getId());
        List<LearnerAiContext.LearnerCourseProgressInfo> activeCourses = progresses.stream()
                .filter(p -> p.getCourse() != null)
                .map(p -> LearnerAiContext.LearnerCourseProgressInfo.builder()
                        .courseId(p.getCourse().getId())
                        .courseTitle(p.getCourse().getTitle())
                        .provider(p.getCourse().getProvider())
                        .difficulty(p.getCourse().getDifficulty() != null ? p.getCourse().getDifficulty().name() : "INTERMEDIATE")
                        .progressPercentage(p.getCompletionPercentage() != null ? p.getCompletionPercentage().intValue() : 0)
                        .timeSpentMinutes(p.getCourse().getDurationMinutes() != null ? p.getCourse().getDurationMinutes() : 0)
                        .build())
                .collect(Collectors.toList());

        List<String> completedTitles = progresses.stream()
                .filter(p -> p.getCourse() != null && p.getCompletionPercentage() != null && p.getCompletionPercentage().doubleValue() >= 100.0)
                .map(p -> p.getCourse().getTitle())
                .collect(Collectors.toList());

        // 4. BKT Mastery and Behavior Profiles
        LearnerMasteryDto.Summary mastery = masteryService.getMasterySummary(user.getId());
        LearnerBehaviorProfile behavior = behaviorService.getBehaviorProfile(user.getId());

        // 5. Metrics
        int streak = behavior.getActiveStreakDays() > 0 ? behavior.getActiveStreakDays() : 0;
        double totalHours = progresses.stream()
                .mapToInt(p -> (p.getCourse() != null && p.getCourse().getDurationMinutes() != null) ? p.getCourse().getDurationMinutes() : 0)
                .sum() / 60.0;

        int readiness = (!skillInfos.isEmpty() || !completedTitles.isEmpty() || mastery.getOverallMasteryPercentage() > 0)
                ? (int) Math.min(100.0, (mastery.getOverallMasteryPercentage() * 0.7) + (completedTitles.size() * 15.0) + (skillInfos.size() * 5.0))
                : 0;

        return LearnerAiContext.builder()
                .userId(user.getId())
                .fullName(user.getFullName() != null ? user.getFullName() : "Learner")
                .targetCareer(user.getTargetCareer() != null ? user.getTargetCareer() : "Software Engineer")
                .experienceLevel(user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "BEGINNER")
                .education(user.getEducation() != null ? user.getEducation() : "Computer Science")
                .personalObjective(user.getPersonalObjective() != null ? user.getPersonalObjective() : "Placement and career preparation")
                .weeklyCommitmentHours(user.getWeeklyCommitmentHours() != null ? user.getWeeklyCommitmentHours() : 10)
                .learningStyle(user.getLearningStyle() != null ? user.getLearningStyle().name() : "VISUAL")
                .preferredContentType(user.getPreferredContentType() != null ? user.getPreferredContentType().name() : "VIDEO")
                .preferredLearningPace(user.getPreferredLearningPace() != null ? user.getPreferredLearningPace() : "6_months")
                .skills(skillInfos)
                .recentAssessments(assessmentSummaries)
                .activeCourses(activeCourses)
                .completedCourseTitles(completedTitles)
                .overallMasteryPercentage(mastery.getOverallMasteryPercentage())
                .masteredSkills(mastery.getMasteredSkills())
                .developingSkills(mastery.getDevelopingSkills())
                .weakSkills(mastery.getWeakSkills())
                .revisionRequiredSkills(mastery.getRevisionRequiredSkills())
                .learningVelocity(behavior.getLearningVelocity())
                .assessmentAccuracy(behavior.getAssessmentAccuracy())
                .preferredDifficulty(behavior.getPreferredDifficulty())
                .activeStreakDays(streak)
                .totalLearningHours(Math.round(totalHours * 10.0) / 10.0)
                .profileCompletionPercentage(user.calculateProfileCompletionPercentage(skillInfos.size()))
                .careerReadinessScore(readiness)
                .build();
    }
}
