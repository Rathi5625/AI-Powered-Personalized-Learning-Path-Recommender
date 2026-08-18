package com.learningpath.learningpath.service;

import com.learningpath.learningpath.dto.AdaptLearningPathResponse;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates the adaptive learning path flow.
 *
 * Sequence:
 * 1. Compute deterministic learner state via {@link LearnerStateService}.
 * 2. Load the learner's persisted active path via {@link LearningPathPersistenceService}.
 * 3. Evaluate whether adaptation is needed via {@link PathChangeDetector} against the REAL current path.
 * 4a. If no change: return the active path with adapted=false (zero AI calls).
 * 4b. If change detected: run {@link PersonalizedLearningPathService} (Gemini + validator + fallback),
 *     which automatically archives the old path and persists the new one as ACTIVE.
 * 5. Return {@link AdaptLearningPathResponse} with full state metadata.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveLearningPathService {

    private final LearnerStateService learnerStateService;
    private final PathChangeDetector pathChangeDetector;
    private final PersonalizedLearningPathService learningPathService;
    private final LearningPathPersistenceService persistenceService;

    /**
     * Adapts the learner's path based on their current deterministic state.
     *
     * @param userId   The learner's UUID.
     * @param careerId The target career UUID.
     * @return An {@link AdaptLearningPathResponse} indicating whether the path was adapted.
     */
    public AdaptLearningPathResponse adapt(UUID userId, UUID careerId) {
        log.info("[AdaptiveLearningPathService] Starting adapt for userId={}, careerId={}", userId, careerId);

        // Step 1: Compute deterministic learner snapshot
        LearnerSnapshot snapshot = learnerStateService.snapshot(userId, careerId);
        List<String> completedSkills = List.copyOf(snapshot.completedSkills());
        List<String> remainingSkills = List.copyOf(snapshot.remainingSkills());

        log.debug("[AdaptiveLearningPathService] Snapshot computed: completedSkills={}, remainingSkills={}",
                completedSkills.size(), remainingSkills.size());

        // Step 2: Load the real persisted active path from database
        Optional<PersonalizedLearningPathResponse> activePathOpt = persistenceService.getActivePathAsPersonalizedResponse(userId);
        PersonalizedLearningPathResponse currentPath = activePathOpt.orElse(null);

        // Step 3: Evaluate change detection against the REAL active path (deterministic, no AI)
        PathChangeDecision decision = pathChangeDetector.detect(snapshot, currentPath);

        if (!decision.shouldAdapt()) {
            // No meaningful change — return the existing active path without invoking AI
            log.info("[AdaptiveLearningPathService] No adaptation needed for userId={}: {}", userId, decision.reason());
            PersonalizedLearningPathResponse responsePath = (currentPath != null)
                    ? currentPath
                    : PersonalizedLearningPathResponse.fail(userId, snapshot.targetCareer(), "No active path found and no adaptation required.");

            return AdaptLearningPathResponse.unchanged(
                    decision.reason(),
                    completedSkills,
                    remainingSkills,
                    responsePath
            );
        }

        // Step 4: Trigger path regeneration through the existing validated pipeline
        // (PersonalizedLearningPathService automatically validates, archives old path, and persists new path)
        log.info("[AdaptiveLearningPathService] Adaptation triggered for userId={}: {}", userId, decision.reason());
        PersonalizedLearningPathResponse newPath = learningPathService.generateLearningPath(userId, careerId);

        return AdaptLearningPathResponse.adapted(
                decision.reason(),
                completedSkills,
                remainingSkills,
                newPath
        );
    }
}
