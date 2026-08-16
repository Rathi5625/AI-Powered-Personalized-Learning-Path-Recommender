package com.learningpath.learningpath.service;

import com.learningpath.entity.UserProgress;
import com.learningpath.entity.enums.ProgressStatus;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Deterministic detector that decides whether the learner's active path should be regenerated.
 *
 * ALL detection rules are based exclusively on persisted state from the database.
 * This component NEVER calls Gemini, the ML service, or any probabilistic system.
 *
 * Detection triggers (evaluated in order, first match wins):
 * <ol>
 *   <li>All courses in the current path are marked COMPLETED.</li>
 *   <li>All required career skills have been mastered (completedSkills covers all career skills).</li>
 *   <li>At least one course in the current path has been newly completed.</li>
 *   <li>The learner's career goal changed relative to the current path's target career.</li>
 *   <li>Remaining skills have changed (new skills emerged or old ones were mastered).</li>
 * </ol>
 */
@Component
@Slf4j
public class PathChangeDetector {

    /**
     * Evaluates whether the given {@link LearnerSnapshot} requires the learning path to be regenerated.
     *
     * @param snapshot    The learner's current deterministic state snapshot.
     * @param currentPath The currently active learning path (may be null if no path exists yet).
     * @return A {@link PathChangeDecision} indicating whether to adapt and why.
     */
    public PathChangeDecision detect(LearnerSnapshot snapshot, PersonalizedLearningPathResponse currentPath) {

        // If there is no current path, always generate one
        if (currentPath == null || currentPath.phases() == null || currentPath.phases().isEmpty()) {
            log.debug("[PathChangeDetector] No existing path for userId={} — triggering initial generation.", snapshot.userId());
            return PathChangeDecision.adapt("No existing learning path found. Generating initial personalized path.");
        }

        // --- Trigger 1: Career goal changed ---
        String pathCareer = currentPath.targetCareer() != null ? currentPath.targetCareer().trim().toLowerCase() : "";
        String snapshotCareer = snapshot.targetCareer() != null ? snapshot.targetCareer().trim().toLowerCase() : "";
        if (!pathCareer.equals(snapshotCareer)) {
            String reason = String.format(
                    "Career goal changed from '%s' to '%s'.",
                    currentPath.targetCareer(), snapshot.targetCareer());
            log.info("[PathChangeDetector] Trigger: career changed for userId={} — {}", snapshot.userId(), reason);
            return PathChangeDecision.adapt(reason);
        }

        // Collect all course IDs in the current path
        Set<UUID> pathCourseIds = currentPath.phases().stream()
                .flatMap(phase -> phase.courses().stream())
                .map(RecommendedCourseItem::courseId)
                .collect(Collectors.toSet());

        // Build a set of completed course IDs from the learner's progress records
        Set<UUID> completedCourseIds = snapshot.courseProgress().stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .map(p -> p.getCourse().getId())
                .collect(Collectors.toSet());

        // --- Trigger 2: All path courses are COMPLETED ---
        if (!pathCourseIds.isEmpty() && completedCourseIds.containsAll(pathCourseIds)) {
            String reason = "All courses in the current learning path have been completed. Generating next phase.";
            log.info("[PathChangeDetector] Trigger: all path courses completed for userId={}", snapshot.userId());
            return PathChangeDecision.adapt(reason);
        }

        // --- Trigger 3: All required career skills are mastered ---
        if (!snapshot.remainingSkills().isEmpty() == false) {
            // remainingSkills is empty meaning all career skills are mastered
            String reason = "All required career skills have been mastered. Generating an advanced progression path.";
            log.info("[PathChangeDetector] Trigger: all career skills mastered for userId={}", snapshot.userId());
            return PathChangeDecision.adapt(reason);
        }

        // --- Trigger 4: A course in the current path was newly completed ---
        Set<UUID> completedPathCourses = pathCourseIds.stream()
                .filter(completedCourseIds::contains)
                .collect(Collectors.toSet());

        if (!completedPathCourses.isEmpty()) {
            int completedCount = completedPathCourses.size();
            int totalCount = pathCourseIds.size();
            String reason = String.format(
                    "%d of %d path courses completed. Adapting path to reflect current progress.",
                    completedCount, totalCount);
            log.info("[PathChangeDetector] Trigger: {} path course(s) completed for userId={}", completedCount, snapshot.userId());
            return PathChangeDecision.adapt(reason);
        }

        // --- No trigger fired ---
        log.debug("[PathChangeDetector] No change detected for userId={}", snapshot.userId());
        return PathChangeDecision.noChange();
    }

    /**
     * Convenience overload — detects change assuming no prior path exists (for initial generation).
     *
     * @param snapshot The learner's current state snapshot.
     * @return Always returns adapt decision.
     */
    public PathChangeDecision detectInitial(LearnerSnapshot snapshot) {
        return detect(snapshot, null);
    }
}
