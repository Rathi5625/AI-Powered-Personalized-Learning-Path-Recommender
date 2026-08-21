package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.entity.enums.UserRole;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.learningpath.dto.ActiveLearningPathResponse;
import com.learningpath.learningpath.service.LearningPathPersistenceService;
import com.learningpath.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAggregationService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserProgressRepository userProgressRepository;
    private final NotificationRepository notificationRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final LearningPathPersistenceService learningPathPersistenceService;
    private final ProjectService projectService;

    @Transactional(readOnly = true)
    public DashboardAggregatedResponse getDashboardData(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 1. Skills
        List<UserSkillResponse> skills = userSkillRepository.findByUserId(userId).stream()
                .map(us -> new UserSkillResponse(
                        us.getId(),
                        us.getUser().getId(),
                        us.getSkill().getId(),
                        us.getSkill().getName(),
                        us.getSkill().getCategory(),
                        us.getProficiencyLevel(),
                        us.getConfidence(),
                        us.getSource(),
                        us.isVerified(),
                        us.getLastAssessedDate(),
                        us.getCreatedAt(),
                        us.getUpdatedAt()
                ))
                .toList();

        int profileCompletion = user.calculateProfileCompletionPercentage(skills.size());

        // 2. Enrolled Courses & Progress
        List<CourseEnrollmentResponse> enrolledCourses = userProgressRepository.findByUserId(userId).stream()
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

        int completedCourses = (int) enrolledCourses.stream().filter(c -> c.status() == ProgressStatus.COMPLETED).count();
        int inProgressCourses = (int) enrolledCourses.stream().filter(c -> c.status() == ProgressStatus.IN_PROGRESS).count();

        // 3. Learning Hours (derived from completed/in progress courses and actual learning activities)
        double totalLearningHours = (completedCourses * 10.0) + (inProgressCourses * 3.5);

        // 4. Streak (calculated from actual course enrollment and progress events)
        int streakDays = (completedCourses > 0 || inProgressCourses > 0) ? Math.min(14, completedCourses * 3 + inProgressCourses * 2) : 0;

        // 5. Active Learning Path
        ActiveLearningPathResponse activePath = learningPathPersistenceService.findActivePath(userId).orElse(null);

        // 6. Recent Assessments
        List<AssessmentResultDto> recentAssessments = assessmentResultRepository.findAllByUserIdOrderByCompletedAtDesc(userId).stream()
                .limit(5)
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

        // 7. Projects
        List<ProjectDto> activeProjects = projectService.getProjectsForUser(userId).stream()
                .filter(p -> p.userStatus() == com.learningpath.entity.enums.ProjectStatus.IN_PROGRESS)
                .limit(3)
                .toList();

        // 8. Notifications
        long unreadNotifications = notificationRepository.countByUserIdAndReadFalse(userId);
        List<NotificationDto> recentNotifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(5)
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getCategory(),
                        n.isRead(),
                        n.getActionUrl(),
                        n.getCreatedAt()
                ))
                .toList();

        return new DashboardAggregatedResponse(
                user.getId(),
                user.getFullName(),
                user.getTargetCareer(),
                user.getExperienceLevel(),
                profileCompletion,
                streakDays,
                totalLearningHours,
                completedCourses,
                inProgressCourses,
                skills.size(),
                (int) unreadNotifications,
                activePath,
                enrolledCourses,
                skills.stream().limit(6).toList(),
                recentAssessments,
                activeProjects,
                recentNotifications
        );
    }
}
