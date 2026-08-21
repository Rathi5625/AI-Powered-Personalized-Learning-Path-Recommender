package com.learningpath.adaptive.service;

import com.learningpath.config.BktConfig;
import com.learningpath.entity.LearnerKnowledgeState;
import com.learningpath.entity.Skill;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.MasteryLevel;
import com.learningpath.repository.LearnerKnowledgeStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BayesianKnowledgeTracingService {

    private final BktConfig bktConfig;
    private final LearnerKnowledgeStateRepository knowledgeStateRepository;

    /**
     * Compute next knowledge probability using standard Bayesian Knowledge Tracing.
     *
     * @param currentPL Current prior probability P(L_t)
     * @param isCorrect Whether learner answered correctly (obs = 1 or obs = 0)
     * @return Updated posterior + transition knowledge probability P(L_{t+1})
     */
    public double computeNextProbability(double currentPL, boolean isCorrect) {
        double pL = Math.max(0.01, Math.min(0.99, currentPL));
        double pG = bktConfig.getGuessProbability();
        double pS = bktConfig.getSlipProbability();
        double pT = bktConfig.getLearnProbability();

        double posterior;
        if (isCorrect) {
            // P(L | correct) = (P(L) * (1 - S)) / (P(L) * (1 - S) + (1 - P(L)) * G)
            double numerator = pL * (1.0 - pS);
            double denominator = numerator + ((1.0 - pL) * pG);
            posterior = numerator / Math.max(1e-6, denominator);
        } else {
            // P(L | incorrect) = (P(L) * S) / (P(L) * S + (1 - P(L)) * (1 - G))
            double numerator = pL * pS;
            double denominator = numerator + ((1.0 - pL) * (1.0 - pG));
            posterior = numerator / Math.max(1e-6, denominator);
        }

        // Apply learning transition: P(L_{t+1}) = P(L_{t|obs}) + (1 - P(L_{t|obs})) * P(T)
        double nextPL = posterior + ((1.0 - posterior) * pT);
        return Math.max(0.01, Math.min(0.99, Math.round(nextPL * 10000.0) / 10000.0));
    }

    /**
     * Determine MasteryLevel from probability based on configuration thresholds.
     */
    public MasteryLevel determineMasteryLevel(double probability) {
        if (probability >= bktConfig.getMasteryThreshold()) {
            return MasteryLevel.MASTERED;
        } else if (probability >= bktConfig.getProficientThreshold()) {
            return MasteryLevel.PROFICIENT;
        } else if (probability >= bktConfig.getBasicThreshold()) {
            return MasteryLevel.BASIC;
        } else if (probability >= bktConfig.getDevelopingThreshold()) {
            return MasteryLevel.DEVELOPING;
        } else {
            return MasteryLevel.NOT_STARTED;
        }
    }

    @Transactional
    public LearnerKnowledgeState updateKnowledgeState(User user, Skill skill, String conceptName, boolean isCorrect, int responseTimeSeconds) {
        String concept = conceptName != null && !conceptName.isBlank() ? conceptName.trim() : (skill != null ? skill.getName() : "General");

        LearnerKnowledgeState state = knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(user.getId(), concept)
                .orElseGet(() -> LearnerKnowledgeState.builder()
                        .user(user)
                        .skill(skill)
                        .conceptName(concept)
                        .knowledgeProbability(bktConfig.getInitialKnowledge())
                        .attempts(0)
                        .correctAttempts(0)
                        .incorrectAttempts(0)
                        .masteryLevel(MasteryLevel.NOT_STARTED)
                        .confidenceScore(0.50)
                        .consecutiveCorrect(0)
                        .consecutiveIncorrect(0)
                        .averageResponseTimeSeconds(0.0)
                        .revisionRequired(false)
                        .build());

        // Compute new BKT probability
        double nextProb = computeNextProbability(state.getKnowledgeProbability(), isCorrect);
        state.setKnowledgeProbability(nextProb);
        state.setAttempts(state.getAttempts() + 1);

        Instant now = Instant.now();
        state.setLastAttemptAt(now);

        if (isCorrect) {
            state.setCorrectAttempts(state.getCorrectAttempts() + 1);
            state.setConsecutiveCorrect(state.getConsecutiveCorrect() + 1);
            state.setConsecutiveIncorrect(0);
            state.setLastCorrectAt(now);
            if (state.getConsecutiveCorrect() >= 2) {
                state.setRevisionRequired(false);
            }
        } else {
            state.setIncorrectAttempts(state.getIncorrectAttempts() + 1);
            state.setConsecutiveIncorrect(state.getConsecutiveIncorrect() + 1);
            state.setConsecutiveCorrect(0);
            if (state.getConsecutiveIncorrect() >= 2 || nextProb < 0.40) {
                state.setRevisionRequired(true);
            }
        }

        // Response time average
        if (state.getAttempts() > 0 && responseTimeSeconds > 0) {
            double currentAvg = state.getAverageResponseTimeSeconds();
            state.setAverageResponseTimeSeconds(((currentAvg * (state.getAttempts() - 1)) + responseTimeSeconds) / state.getAttempts());
        }

        // Mastery Level
        MasteryLevel level = determineMasteryLevel(nextProb);
        state.setMasteryLevel(level);

        // Confidence metric
        double sampleFactor = Math.min(1.0, state.getAttempts() / 10.0);
        double accuracy = (double) state.getCorrectAttempts() / Math.max(1, state.getAttempts());
        double confidence = (nextProb * 0.5) + (accuracy * 0.3) + (sampleFactor * 0.2);
        state.setConfidenceScore(Math.round(confidence * 100.0) / 100.0);

        log.info("[BKT Update] User: {}, Concept: {}, Correct: {}, New P(L): {}, Level: {}",
                user.getId(), concept, isCorrect, nextProb, level);

        return knowledgeStateRepository.save(state);
    }
}
