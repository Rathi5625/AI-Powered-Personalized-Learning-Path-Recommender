package com.learningpath.learningpath.service;

import com.learningpath.learningpath.dto.AdaptLearningPathResponse;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the adaptive learning path flow.
 *
 * Sequence:
 * 1. Compute deterministic learner state via {@link LearnerStateService}.
 * 2. Evaluate whether adaptation is needed via {@link PathChangeDetector}.
 * 3a. If no change: return the current context with adapted=false.
 * 3b. If change detected: run {@link PersonalizedLearningPathService} (Gemini + validator + fallback).
 * 4. Return {@link AdaptLearningPathResponse} with full state metadata.
 *
 * Gemini is called ONLY in step 3b, and only for generating explanation text and path structure.
 * The adaptation decision, skill lists, and mastery state are ALL deterministic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveLearningPathService {

    private final LearnerStateService learnerStateService;
    private final PathChangeDetector pathChangeDetector;
    private final PersonalizedLearningPathService learningPathService;

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

        // Step 2: Evaluate change detection (deterministic, no AI)
        // We pass null as currentPath because this endpoint doesn't persist paths in DB yet;
        // if all courses in snapshot are not completed, the detector falls back to checking progress.
        // If the learner has completed any course, the detector will trigger regeneration.
        PathChangeDecision decision = pathChangeDetector.detect(snapshot, null);

        if (!decision.shouldAdapt()) {
            // No meaningful change — generate a fresh path anyway on first call
            // (null currentPath means "initial generation" always triggers adaptation)
            // This branch is reached only if detect() returns noChange() which it won't for null path.
            // Keeping this for correctness when integration with persisted path state is added later.
            log.info("[AdaptiveLearningPathService] No adaptation needed for userId={}: {}", userId, decision.reason());
            return AdaptLearningPathResponse.unchanged(
                    decision.reason(),
                    completedSkills,
                    remainingSkills,
                    PersonalizedLearningPathResponse.fail(userId, snapshot.targetCareer(),
                            "No adaptation required — current path remains optimal.")
            );
        }

        // Step 3: Trigger path regeneration through the existing validated pipeline
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
