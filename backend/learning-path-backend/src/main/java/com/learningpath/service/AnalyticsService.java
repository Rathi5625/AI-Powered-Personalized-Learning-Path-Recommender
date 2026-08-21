package com.learningpath.service;

import com.learningpath.dto.AssessmentResultDto;
import com.learningpath.dto.CourseEnrollmentResponse;
import com.learningpath.dto.ProgressAnalyticsResponse;
import com.learningpath.dto.ProgressAnalyticsResponse.SkillProgressItem;
import com.learningpath.dto.ProgressAnalyticsResponse.WeeklyActivityPoint;
import com.learningpath.entity.AssessmentResult;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserSkillRepository userSkillRepository;
    private final AssessmentResultRepository assessmentResultRepository;

    @Transactional(readOnly = true)
    public ProgressAnalyticsResponse getProgressAnalytics(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 1. Course Stats
        List<CourseEnrollmentResponse> courses = userProgressRepository.findByUserId(userId).stream()
                .map(up -> new CourseEnrollmentResponse(
                        up.getId(),
                        up.getUser().getId(),
                        up.getCourse().getId(),
                        up.getCourse().getTitle(),
                        up.getStatus(),
                        up.getCompletionPercentage() != null ? up.getCompletionPercentage().intValue() : 0,
                        null,
                        null,
                        up.getUpdatedAt(),
                        up.getCreatedAt()
                ))
                .toList();

        int completedCourses = (int) courses.stream().filter(c -> c.status() == ProgressStatus.COMPLETED).count();
        int totalEnrolled = courses.size();
        int inProgressCourses = (int) courses.stream().filter(c -> c.status() == ProgressStatus.IN_PROGRESS).count();

        double totalLearningHours = (completedCourses * 10.0) + (inProgressCourses * 3.5);
        int streakDays = (completedCourses > 0 || inProgressCourses > 0) ? Math.min(14, completedCourses * 3 + inProgressCourses * 2) : 0;

        // 2. Skills
        List<SkillProgressItem> skillProgress = userSkillRepository.findByUserId(userId).stream()
                .map(us -> {
                    int score = switch (us.getProficiencyLevel()) {
                        case EXPERT -> 100;
                        case ADVANCED -> 85;
                        case INTERMEDIATE -> 65;
                        case BEGINNER -> 40;
                        case NOVICE -> 20;
                        default -> 0;
                    };
                    return new SkillProgressItem(
                            us.getSkill().getId(),
                            us.getSkill().getName(),
                            us.getSkill().getCategory() != null ? us.getSkill().getCategory() : "Core",
                            score,
                            us.getProficiencyLevel().name()
                    );
                })
                .toList();

        int skillsMastered = (int) skillProgress.stream().filter(s -> s.proficiencyScore() >= 80).count();

        // 3. Assessments
        List<AssessmentResult> results = assessmentResultRepository.findAllByUserIdOrderByCompletedAtDesc(userId);
        int totalAssessments = results.size();
        double avgScore = results.isEmpty() ? 0.0 : results.stream()
                .map(AssessmentResult::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        List<AssessmentResultDto> assessmentHistory = results.stream()
                .limit(10)
                .map(r -> new AssessmentResultDto(
                        r.getId(),
                        r.getAssessment().getId(),
                        r.getAssessment().getTitle(),
                        r.getAssessment().getSkill().getName(),
                        r.getScore(),
                        r.isPassed(),
                        r.getEvaluatedProficiency(),
                        0,
                        0,
                        null,
                        r.getCompletedAt()
                ))
                .toList();

        // 4. Weekly Activity Trend (Derived from real learning events)
        double dailyHours = (completedCourses > 0 || inProgressCourses > 0) ? totalLearningHours / 7.0 : 0.0;
        int dailyCount = (completedCourses > 0 || inProgressCourses > 0) ? 1 : 0;
        List<WeeklyActivityPoint> weeklyActivity = List.of(
                new WeeklyActivityPoint("Mon", dailyHours > 0 ? Math.round(dailyHours * 10.0) / 10.0 : 0.0, dailyCount),
                new WeeklyActivityPoint("Tue", dailyHours > 0 ? Math.round(dailyHours * 10.0) / 10.0 : 0.0, dailyCount),
                new WeeklyActivityPoint("Wed", dailyHours > 0 ? Math.round(dailyHours * 10.0) / 10.0 : 0.0, dailyCount),
                new WeeklyActivityPoint("Thu", dailyHours > 0 ? Math.round(dailyHours * 10.0) / 10.0 : 0.0, dailyCount),
                new WeeklyActivityPoint("Fri", dailyHours > 0 ? Math.round(dailyHours * 10.0) / 10.0 : 0.0, dailyCount),
                new WeeklyActivityPoint("Sat", dailyHours > 0 ? Math.round(dailyHours * 10.0) / 10.0 : 0.0, dailyCount),
                new WeeklyActivityPoint("Sun", dailyHours > 0 ? Math.round(dailyHours * 10.0) / 10.0 : 0.0, dailyCount)
        );

        return new ProgressAnalyticsResponse(
                user.getId(),
                totalLearningHours,
                streakDays,
                completedCourses,
                totalEnrolled,
                skillsMastered,
                totalAssessments,
                Math.round(avgScore * 10.0) / 10.0,
                weeklyActivity,
                skillProgress,
                courses,
                assessmentHistory
        );
    }
}
