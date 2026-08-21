package com.learningpath.adaptive.service;

import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.repository.LearnerKnowledgeStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveDifficultyService {

    private final LearnerKnowledgeStateRepository knowledgeStateRepository;

    /**
     * Determines the next difficulty level with smoothing to prevent wild single-answer oscillations.
     *
     * @param userId Learner UUID
     * @param conceptName Target skill / concept
     * @param currentDifficulty Current difficulty level
     * @param isCorrect Correctness of the last answer
     * @param responseTimeSeconds Time spent on the last question
     * @param streakCorrect Consecutive correct count
     * @param streakIncorrect Consecutive incorrect count
     * @return Smoothed next CourseDifficulty
     */
    public CourseDifficulty determineNextDifficulty(
            UUID userId,
            String conceptName,
            CourseDifficulty currentDifficulty,
            boolean isCorrect,
            int responseTimeSeconds,
            int streakCorrect,
            int streakIncorrect
    ) {
        CourseDifficulty current = (currentDifficulty != null) ? currentDifficulty : CourseDifficulty.BEGINNER;

        double prob = 0.50;
        if (userId != null && conceptName != null && !conceptName.isBlank()) {
            Optional<LearnerKnowledgeState> stateOpt = knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(userId, conceptName);
            if (stateOpt.isPresent()) {
                prob = stateOpt.get().getKnowledgeProbability();
            }
        }

        // Case 1: Repeated failures or low mastery -> Step down
        if (streakIncorrect >= 2 || (!isCorrect && prob < 0.40)) {
            return stepDown(current);
        }

        // Case 2: Correct + fast response (<15s) + high streak or high mastery -> Step up
        if (isCorrect && (streakCorrect >= 2 || (prob >= 0.70 && responseTimeSeconds < 25))) {
            return stepUp(current);
        }

        // Case 3: Correct but slow (>60s) -> Maintain difficulty to build fluency
        if (isCorrect && responseTimeSeconds > 60) {
            return current;
        }

        // Case 4: Single incorrect at advanced level -> Step down to intermediate
        if (!isCorrect && current == CourseDifficulty.ADVANCED) {
            return CourseDifficulty.INTERMEDIATE;
        }

        // Default: Maintain current difficulty
        return current;
    }

    /**
     * Determine initial or baseline difficulty for a concept.
     */
    public CourseDifficulty determineDifficulty(UUID userId, String conceptName, CourseDifficulty defaultDifficulty) {
        if (userId == null) {
            return defaultDifficulty != null ? defaultDifficulty : CourseDifficulty.BEGINNER;
        }

        Optional<LearnerKnowledgeState> stateOpt = conceptName != null && !conceptName.isBlank()
                ? knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(userId, conceptName)
                : Optional.empty();

        if (stateOpt.isEmpty()) {
            return defaultDifficulty != null ? defaultDifficulty : CourseDifficulty.BEGINNER;
        }

        LearnerKnowledgeState state = stateOpt.get();
        double prob = state.getKnowledgeProbability();
        int streakCorrect = state.getConsecutiveCorrect();
        int streakIncorrect = state.getConsecutiveIncorrect();

        if (streakIncorrect >= 2 || prob < 0.35) {
            return CourseDifficulty.BEGINNER;
        }

        if (streakCorrect >= 3 && prob >= 0.75) {
            return CourseDifficulty.ADVANCED;
        }

        if (prob >= 0.50) {
            return CourseDifficulty.INTERMEDIATE;
        }

        return CourseDifficulty.BEGINNER;
    }

    private CourseDifficulty stepUp(CourseDifficulty current) {
        if (current == null) return CourseDifficulty.INTERMEDIATE;
        return switch (current) {
            case BEGINNER, EASY -> CourseDifficulty.INTERMEDIATE;
            case INTERMEDIATE, MEDIUM -> CourseDifficulty.ADVANCED;
            case ADVANCED, HIGH, ALL_LEVELS -> CourseDifficulty.ADVANCED;
            default -> CourseDifficulty.ADVANCED;
        };
    }

    private CourseDifficulty stepDown(CourseDifficulty current) {
        if (current == null) return CourseDifficulty.BEGINNER;
        return switch (current) {
            case ADVANCED, HIGH -> CourseDifficulty.INTERMEDIATE;
            case INTERMEDIATE, MEDIUM -> CourseDifficulty.BEGINNER;
            case BEGINNER, EASY, ALL_LEVELS -> CourseDifficulty.BEGINNER;
            default -> CourseDifficulty.BEGINNER;
        };
    }

}
