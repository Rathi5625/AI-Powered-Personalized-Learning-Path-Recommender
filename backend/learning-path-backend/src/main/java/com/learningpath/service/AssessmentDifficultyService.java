package com.learningpath.service;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.ExperienceLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AssessmentDifficultyService {

    /**
     * Determines next recommended question difficulty based on prior answer correctness and user level.
     * Phase 2 deterministic heuristic:
     * - If correct consecutive answers >= 2: increase difficulty.
     * - If incorrect consecutive answers >= 1: decrease or maintain difficulty.
     * (Isolated service ready to be swapped with ML / Bayesian Knowledge Tracing in Phase 7).
     */
    public CourseDifficulty determineNextDifficulty(
            CourseDifficulty currentDifficulty,
            boolean isCorrect,
            int consecutiveCorrect,
            int consecutiveIncorrect,
            ExperienceLevel experienceLevel
    ) {
        if (currentDifficulty == null) {
            if (experienceLevel == ExperienceLevel.ADVANCED) return CourseDifficulty.ADVANCED;
            if (experienceLevel == ExperienceLevel.INTERMEDIATE) return CourseDifficulty.INTERMEDIATE;
            return CourseDifficulty.BEGINNER;
        }

        if (isCorrect && consecutiveCorrect >= 2) {
            if (currentDifficulty == CourseDifficulty.BEGINNER) {
                return CourseDifficulty.INTERMEDIATE;
            } else if (currentDifficulty == CourseDifficulty.INTERMEDIATE) {
                return CourseDifficulty.ADVANCED;
            }
        } else if (!isCorrect && consecutiveIncorrect >= 1) {
            if (currentDifficulty == CourseDifficulty.ADVANCED) {
                return CourseDifficulty.INTERMEDIATE;
            } else if (currentDifficulty == CourseDifficulty.INTERMEDIATE) {
                return CourseDifficulty.BEGINNER;
            }
        }

        return currentDifficulty;
    }
}
