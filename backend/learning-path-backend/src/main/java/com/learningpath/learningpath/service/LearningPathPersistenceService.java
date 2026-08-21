package com.learningpath.learningpath.service;

import com.learningpath.entity.*;
import com.learningpath.entity.enums.LearningPathStatus;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.learningpath.dto.*;
import com.learningpath.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing the database persistence and active lifecycle of learning paths.
 *
 * Invariants enforced:
 * - A learner has at most ONE ACTIVE learning path at any time.
 * - Generating or adapting a path archives previous ACTIVE paths.
 * - Path history remains accessible for auditing and frontend tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathPersistenceService {

    private final LearningPathRepository learningPathRepository;
    private final LearningPathItemRepository learningPathItemRepository;
    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final CourseRepository courseRepository;
    private final CourseSkillRepository courseSkillRepository;
    private final UserProgressRepository userProgressRepository;

    /**
     * Persists a validated learning path as the user's new ACTIVE path.
     * Automatically archives any existing active learning paths for the user.
     *
     * @param userId   The learner's UUID.
     * @param careerId The target career UUID (optional).
     * @param response The validated learning path response to persist.
     * @return The persisted {@link LearningPath} entity.
     */
    @Transactional
    public LearningPath saveLearningPath(UUID userId, UUID careerId, PersonalizedLearningPathResponse response) {
        log.info("[LearningPathPersistenceService] Persisting new ACTIVE learning path for userId={}, careerId={}", userId, careerId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Career career = (careerId != null) ? careerRepository.findById(careerId).orElse(null) : null;

        // Step 1: Archive previous active paths
        int archivedCount = learningPathRepository.updateStatusByUserIdAndStatus(userId, LearningPathStatus.ACTIVE, LearningPathStatus.ARCHIVED);
        if (archivedCount > 0) {
            log.info("[LearningPathPersistenceService] Archived {} previous active path(s) for userId={}", archivedCount, userId);
        }

        // Step 2: Create and persist the new active LearningPath
        String targetCareerTitle = (career != null) ? career.getTitle() : response.targetCareer();
        String title = (targetCareerTitle != null && !targetCareerTitle.isBlank())
                ? "Personalized Learning Path for " + targetCareerTitle
                : "Personalized Learning Path";

        LearningPath learningPath = LearningPath.builder()
                .user(user)
                .targetCareer(career)
                .title(title)
                .description(response.summary())
                .status(LearningPathStatus.ACTIVE)
                .build();

        LearningPath savedPath = learningPathRepository.save(learningPath);

        // Step 3: Persist all LearningPathItems ordered by phase and sequence
        int globalOrder = 1;
        if (response.phases() != null) {
            for (LearningPathPhase phase : response.phases()) {
                if (phase.courses() == null) continue;

                for (RecommendedCourseItem courseItem : phase.courses()) {
                    if (courseItem.courseId() == null) continue;

                    Optional<Course> courseOpt = courseRepository.findById(courseItem.courseId());
                    if (courseOpt.isEmpty()) {
                        log.warn("[LearningPathPersistenceService] Skipping ungrounded course ID: {}", courseItem.courseId());
                        continue;
                    }

                    Course course = courseOpt.get();

                    // Check if learner has already completed this course in UserProgress
                    boolean isCompleted = userProgressRepository
                            .findByUserIdAndCourseId(userId, course.getId())
                            .map(p -> p.getStatus() == ProgressStatus.COMPLETED)
                            .orElse(false);

                    LearningPathItem item = LearningPathItem.builder()
                            .learningPath(savedPath)
                            .course(course)
                            .phaseNumber(phase.phaseNumber())
                            .phaseTitle(phase.phaseTitle())
                            .estimatedDuration(phase.estimatedDuration())
                            .explanation(phase.explanation())
                            .itemOrder(globalOrder++)
                            .isCompleted(isCompleted)
                            .build();

                    learningPathItemRepository.save(item);
                }
            }
        }

        log.info("[LearningPathPersistenceService] Successfully persisted path id={} with {} items for userId={}",
                savedPath.getId(), globalOrder - 1, userId);

        return savedPath;
    }

    /**
     * Finds the learner's active learning path without throwing when not found.
     *
     * @param userId The learner's UUID.
     * @return Optional containing the active path response if one exists, empty otherwise.
     */
    @Transactional(readOnly = true)
    public Optional<ActiveLearningPathResponse> findActivePath(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)
                .map(this::buildActivePathResponse);
    }

    /**
     * Retrieves the learner's active learning path.
     *
     * @param userId The learner's UUID.
     * @return {@link ActiveLearningPathResponse} containing path details and phased courses.
     * @throws ResourceNotFoundException if user or active path does not exist.
     */
    @Transactional(readOnly = true)
    public ActiveLearningPathResponse getActivePath(UUID userId) {
        return findActivePath(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No active learning path found for user id: " + userId));
    }

    /**
     * Reconstructs the {@link PersonalizedLearningPathResponse} representation of the active path.
     * Used by {@link AdaptiveLearningPathService} to pass the real current path to {@link PathChangeDetector}.
     *
     * @param userId The learner's UUID.
     * @return Optional containing the active path representation, or empty if none active.
     */
    @Transactional(readOnly = true)
    public Optional<PersonalizedLearningPathResponse> getActivePathAsPersonalizedResponse(UUID userId) {
        return learningPathRepository.findByUserIdAndStatus(userId, LearningPathStatus.ACTIVE)
                .map(activePath -> {
                    List<LearningPathPhase> phases = buildPhasesFromItems(activePath.getId());
                    String targetCareer = (activePath.getTargetCareer() != null)
                            ? activePath.getTargetCareer().getTitle()
                            : "Software Engineering";

                    return PersonalizedLearningPathResponse.ok(
                            userId,
                            targetCareer,
                            activePath.getDescription(),
                            phases,
                            "PERSISTED_DB",
                            "Active Database Path"
                    );
                });
    }

    /**
     * Retrieves the user's historical learning paths ordered newest first.
     *
     * @param userId The learner's UUID.
     * @return List of {@link LearningPathSummaryResponse}.
     */
    @Transactional(readOnly = true)
    public List<LearningPathSummaryResponse> getPathHistory(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<LearningPath> paths = learningPathRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return paths.stream().map(path -> {
            List<LearningPathItem> items = learningPathItemRepository.findByLearningPathIdOrderByPhaseNumberAscItemOrderAsc(path.getId());
            Set<Integer> phaseNumbers = items.stream()
                    .map(LearningPathItem::getPhaseNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            String targetCareer = (path.getTargetCareer() != null) ? path.getTargetCareer().getTitle() : "Software Engineering";

            return new LearningPathSummaryResponse(
                    path.getId(),
                    userId,
                    targetCareer,
                    path.getTitle(),
                    path.getDescription(),
                    path.getStatus(),
                    phaseNumbers.isEmpty() ? 1 : phaseNumbers.size(),
                    items.size(),
                    path.getCreatedAt(),
                    path.getUpdatedAt()
            );
        }).toList();
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private ActiveLearningPathResponse buildActivePathResponse(LearningPath activePath) {
        List<LearningPathPhase> phases = buildPhasesFromItems(activePath.getId());
        int totalCourses = phases.stream().mapToInt(p -> p.courses().size()).sum();
        String targetCareer = (activePath.getTargetCareer() != null) ? activePath.getTargetCareer().getTitle() : "Software Engineering";

        return new ActiveLearningPathResponse(
                activePath.getId(),
                activePath.getUser().getId(),
                targetCareer,
                activePath.getTitle(),
                activePath.getDescription(),
                activePath.getStatus(),
                phases.size(),
                totalCourses,
                phases,
                activePath.getCreatedAt(),
                activePath.getUpdatedAt()
        );
    }

    private List<LearningPathPhase> buildPhasesFromItems(UUID learningPathId) {
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathIdOrderByPhaseNumberAscItemOrderAsc(learningPathId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // Group items by phaseNumber preserving phase sequence
        Map<Integer, List<LearningPathItem>> phaseMap = new LinkedHashMap<>();
        for (LearningPathItem item : items) {
            int phaseNum = (item.getPhaseNumber() != null) ? item.getPhaseNumber() : 1;
            phaseMap.computeIfAbsent(phaseNum, k -> new ArrayList<>()).add(item);
        }

        List<LearningPathPhase> phases = new ArrayList<>();
        for (Map.Entry<Integer, List<LearningPathItem>> entry : phaseMap.entrySet()) {
            int phaseNum = entry.getKey();
            List<LearningPathItem> phaseItems = entry.getValue();

            String phaseTitle = phaseItems.get(0).getPhaseTitle() != null ? phaseItems.get(0).getPhaseTitle() : "Phase " + phaseNum;
            String duration = phaseItems.get(0).getEstimatedDuration() != null ? phaseItems.get(0).getEstimatedDuration() : "2 weeks";
            String explanation = phaseItems.get(0).getExplanation() != null ? phaseItems.get(0).getExplanation() : "Milestone courses";

            List<RecommendedCourseItem> courses = new ArrayList<>();
            Set<String> targetSkills = new LinkedHashSet<>();

            for (LearningPathItem item : phaseItems) {
                Course course = item.getCourse();

                List<String> matchedSkills = courseSkillRepository.findByCourseId(course.getId()).stream()
                        .map(cs -> cs.getSkill().getName())
                        .distinct()
                        .toList();

                targetSkills.addAll(matchedSkills);

                courses.add(new RecommendedCourseItem(
                        course.getId(),
                        course.getTitle(),
                        course.getProvider(),
                        course.getRating() != null ? course.getRating().doubleValue() : 4.5,
                        course.getDifficulty() != null ? course.getDifficulty().name() : "BEGINNER",
                        matchedSkills
                ));
            }

            phases.add(new LearningPathPhase(
                    phaseNum,
                    phaseTitle,
                    new ArrayList<>(targetSkills),
                    courses,
                    duration,
                    explanation
            ));
        }

        return phases;
    }
}
