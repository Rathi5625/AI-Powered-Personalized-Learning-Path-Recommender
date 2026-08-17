package com.learningpath.service;

import com.learningpath.dto.LearningProgressRequest;
import com.learningpath.dto.LearningProgressResponse;
import com.learningpath.entity.Course;
import com.learningpath.entity.User;
import com.learningpath.entity.UserProgress;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.UserProgressRepository;
import com.learningpath.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for persisting and retrieving learner course progress.
 *
 * All operations are deterministic — no AI, no ML calls.
 * The service guarantees idempotency: calling upsertProgress with the same
 * (userId, courseId) updates the existing record rather than creating a duplicate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserProgressRepository progressRepository;

    /**
     * Creates or updates the learner's progress for a specific course.
     *
     * Invariants enforced:
     * - When status = COMPLETED, completionPercentage is automatically set to 100.
     * - When status = NOT_STARTED, completionPercentage is automatically set to 0.
     * - lastAccessedAt is always updated to the current instant.
     *
     * @param userId   The learner's UUID.
     * @param courseId The course UUID.
     * @param request  The progress update request.
     * @return The persisted (created or updated) progress record.
     */
    @Transactional
    public LearningProgressResponse upsertProgress(UUID userId, UUID courseId, LearningProgressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        UserProgress progress = progressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> UserProgress.builder()
                        .user(user)
                        .course(course)
                        .build());

        // Enforce invariants on completion percentage
        BigDecimal effectivePercentage = resolveCompletionPercentage(request.status(), request.completionPercentage());

        progress.setStatus(request.status());
        progress.setCompletionPercentage(effectivePercentage);
        progress.setLastAccessedAt(Instant.now());

        UserProgress saved = progressRepository.save(progress);
        log.info("[UserProgressService] Upserted progress for userId={}, courseId={}, status={}, completion={}%",
                userId, courseId, saved.getStatus(), saved.getCompletionPercentage());

        return toResponse(saved);
    }

    /**
     * Returns all course progress records for a learner.
     *
     * @param userId The learner's UUID.
     * @return List of progress responses for all enrolled courses.
     */
    @Transactional(readOnly = true)
    public List<LearningProgressResponse> getUserProgress(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return progressRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns the progress record for a specific course.
     *
     * @param userId   The learner's UUID.
     * @param courseId The course UUID.
     * @return The progress response for the specified course.
     */
    @Transactional(readOnly = true)
    public LearningProgressResponse getCourseProgress(UUID userId, UUID courseId) {
        UserProgress progress = progressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No progress record found for userId=%s and courseId=%s", userId, courseId)));
        return toResponse(progress);
    }

    /**
     * Calculates aggregated learning progress summary metrics for the learner.
     *
     * @param userId The learner's UUID.
     * @return {@link com.learningpath.dto.UserProgressSummaryResponse} with overall completion stats.
     */
    @Transactional(readOnly = true)
    public com.learningpath.dto.UserProgressSummaryResponse getProgressSummary(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<UserProgress> progressList = progressRepository.findByUserId(userId);
        if (progressList.isEmpty()) {
            return new com.learningpath.dto.UserProgressSummaryResponse(0, 0, 0, 0, 0, 0.0);
        }

        int completed = 0;
        int inProgress = 0;
        int notStarted = 0;
        int paused = 0;
        double totalPercentage = 0.0;

        for (UserProgress p : progressList) {
            ProgressStatus status = p.getStatus();
            if (status == ProgressStatus.COMPLETED) {
                completed++;
                totalPercentage += 100.0;
            } else if (status == ProgressStatus.IN_PROGRESS) {
                inProgress++;
                totalPercentage += (p.getCompletionPercentage() != null ? p.getCompletionPercentage().doubleValue() : 0.0);
            } else if (status == ProgressStatus.PAUSED) {
                paused++;
                totalPercentage += (p.getCompletionPercentage() != null ? p.getCompletionPercentage().doubleValue() : 0.0);
            } else {
                notStarted++;
                totalPercentage += 0.0;
            }
        }

        int totalTracked = progressList.size();
        double overallRate = Math.round((totalPercentage / totalTracked) * 100.0) / 100.0;

        return new com.learningpath.dto.UserProgressSummaryResponse(
                totalTracked,
                completed,
                inProgress,
                notStarted,
                paused,
                overallRate
        );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves the effective completion percentage based on the given status.
     * COMPLETED always returns 100. NOT_STARTED always returns 0.
     * Otherwise, the provided value is used (defaulting to 0 if null).
     */
    private BigDecimal resolveCompletionPercentage(ProgressStatus status, BigDecimal requested) {
        if (status == ProgressStatus.COMPLETED) {
            return BigDecimal.valueOf(100);
        }
        if (status == ProgressStatus.NOT_STARTED) {
            return BigDecimal.ZERO;
        }
        return (requested != null) ? requested : BigDecimal.ZERO;
    }

    private LearningProgressResponse toResponse(UserProgress p) {
        return new LearningProgressResponse(
                p.getId(),
                p.getUser().getId(),
                p.getCourse().getId(),
                p.getCourse().getTitle(),
                p.getStatus(),
                p.getCompletionPercentage(),
                p.getLastAccessedAt(),
                p.getUpdatedAt()
        );
    }
}
