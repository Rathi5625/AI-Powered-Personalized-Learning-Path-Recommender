package com.learningpath.adaptive.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.adaptive.dto.AdaptiveAssessmentDto;
import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.adaptive.dto.LearnerMasteryDto;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.*;
import com.learningpath.learningpath.service.LearningPathRecalculationService;
import com.learningpath.repository.*;
import com.learningpath.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdaptiveAssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AdaptiveAssessmentSessionRepository sessionRepository;
    private final AdaptiveAssessmentResponseRepository responseRepository;
    private final LearnerKnowledgeStateRepository knowledgeStateRepository;
    private final AdaptiveDifficultyService difficultyService;
    private final BayesianKnowledgeTracingService bktService;
    private final LearnerBehaviorService behaviorService;
    private final LearnerMasteryService masteryService;
    private final LearningPathRecalculationService recalculationService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final int MIN_QUESTIONS = 5;
    public static final int MAX_QUESTIONS = 15;

    @Transactional
    public AdaptiveAssessmentDto.SessionStartResponse startSession(UUID assessmentId, User user) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        String skillName = assessment.getSkill() != null ? assessment.getSkill().getName() : "General";
        CourseDifficulty initialDifficulty = difficultyService.determineDifficulty(
                user != null ? user.getId() : null,
                skillName,
                CourseDifficulty.BEGINNER
        );

        List<AssessmentQuestion> allQuestions = questionRepository.findAllByAssessmentId(assessmentId);
        if (allQuestions.isEmpty()) {
            throw new IllegalStateException("No questions available for assessment: " + assessmentId);
        }

        AdaptiveAssessmentSession session = AdaptiveAssessmentSession.builder()
                .user(user)
                .assessment(assessment)
                .startedAt(Instant.now())
                .status(AdaptiveSessionStatus.IN_PROGRESS)
                .currentDifficulty(initialDifficulty)
                .questionsAsked(0)
                .correctAnswers(0)
                .incorrectAnswers(0)
                .averageResponseTimeSeconds(0.0)
                .currentAbilityEstimate(0.50)
                .confidenceScore(0.50)
                .confidenceLevel(ConfidenceLevel.LOW)
                .build();

        session = sessionRepository.save(session);
        log.info("[AdaptiveAssessmentService] Started session={} for user={} on assessment={}", session.getId(), user.getId(), assessment.getTitle());

        return AdaptiveAssessmentDto.SessionStartResponse.builder()
                .sessionId(session.getId())
                .assessmentId(assessment.getId())
                .assessmentTitle(assessment.getTitle())
                .skillName(skillName)
                .currentDifficulty(initialDifficulty)
                .status(session.getStatus())
                .startedAt(session.getStartedAt())
                .totalAvailableQuestions(allQuestions.size())
                .build();
    }

    @Transactional
    public AdaptiveAssessmentDto.NextQuestionResponse getNextQuestion(UUID sessionId, User user) {
        AdaptiveAssessmentSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Active assessment session not found: " + sessionId));

        if (session.getStatus() == AdaptiveSessionStatus.COMPLETED) {
            return AdaptiveAssessmentDto.NextQuestionResponse.builder()
                    .sessionId(sessionId)
                    .isTerminated(true)
                    .terminationReason(session.getTerminationReason() != null ? session.getTerminationReason() : "Assessment already completed.")
                    .build();
        }

        List<AdaptiveAssessmentResponse> answeredList = responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId);
        Set<UUID> answeredQuestionIds = answeredList.stream()
                .map(r -> r.getQuestion().getId())
                .collect(Collectors.toSet());

        // Check Intelligent Stopping Criteria
        if (shouldStopAssessment(session, answeredList)) {
            session.setStatus(AdaptiveSessionStatus.COMPLETED);
            session.setCompletedAt(Instant.now());
            if (session.getTerminationReason() == null) {
                session.setTerminationReason(deriveTerminationReason(session, answeredList));
            }
            sessionRepository.save(session);
            triggerPostCompletionWorkflow(session, user);

            return AdaptiveAssessmentDto.NextQuestionResponse.builder()
                    .sessionId(sessionId)
                    .isTerminated(true)
                    .terminationReason(session.getTerminationReason())
                    .build();
        }

        // CAT-Style Question Selection
        List<AssessmentQuestion> allQuestions = questionRepository.findAllByAssessmentId(session.getAssessment().getId());
        List<AssessmentQuestion> availableQuestions = allQuestions.stream()
                .filter(q -> !answeredQuestionIds.contains(q.getId()))
                .toList();

        if (availableQuestions.isEmpty()) {
            session.setStatus(AdaptiveSessionStatus.COMPLETED);
            session.setCompletedAt(Instant.now());
            session.setTerminationReason("All configured questions in pool completed.");
            sessionRepository.save(session);
            triggerPostCompletionWorkflow(session, user);

            return AdaptiveAssessmentDto.NextQuestionResponse.builder()
                    .sessionId(sessionId)
                    .isTerminated(true)
                    .terminationReason(session.getTerminationReason())
                    .build();
        }

        AssessmentQuestion selectedQuestion = selectBestCatQuestion(availableQuestions, session, user);

        List<String> options = List.of();
        if (selectedQuestion.getOptionsJson() != null && !selectedQuestion.getOptionsJson().isBlank()) {
            try {
                options = objectMapper.readValue(selectedQuestion.getOptionsJson(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse options JSON for question {}: {}", selectedQuestion.getId(), e.getMessage());
            }
        }

        String skillName = session.getAssessment().getSkill() != null ? session.getAssessment().getSkill().getName() : "General";

        return AdaptiveAssessmentDto.NextQuestionResponse.builder()
                .sessionId(sessionId)
                .questionId(selectedQuestion.getId().toString())
                .questionNumber(session.getQuestionsAsked() + 1)
                .totalQuestionsEstimated(Math.min(MAX_QUESTIONS, allQuestions.size()))
                .questionText(selectedQuestion.getQuestionText())
                .questionType(selectedQuestion.getQuestionType() != null ? selectedQuestion.getQuestionType().name() : "SINGLE_CHOICE")
                .options(options)
                .difficulty(selectedQuestion.getDifficulty())
                .skillName(skillName)
                .conceptFocus(skillName)
                .isTerminated(false)
                .build();
    }

    @Transactional
    public AdaptiveAssessmentDto.AnswerSubmissionResult submitAnswer(
            UUID sessionId,
            AdaptiveAssessmentDto.AnswerSubmissionRequest request,
            User user
    ) {
        AdaptiveAssessmentSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Assessment session not found: " + sessionId));

        if (session.getStatus() == AdaptiveSessionStatus.COMPLETED) {
            throw new IllegalStateException("Assessment session is already completed.");
        }

        UUID questionId = UUID.fromString(request.getQuestionId());
        AssessmentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        String expected = question.getCorrectAnswer() != null ? question.getCorrectAnswer().trim() : "";
        String provided = request.getAnswer() != null ? request.getAnswer().trim() : "";
        boolean isCorrect = expected.equalsIgnoreCase(provided);

        String concept = session.getAssessment().getSkill() != null ? session.getAssessment().getSkill().getName() : "General";
        LearnerKnowledgeState stateBefore = knowledgeStateRepository.findByUserIdAndConceptNameIgnoreCase(user.getId(), concept)
                .orElse(null);
        double probBefore = stateBefore != null ? stateBefore.getKnowledgeProbability() : 0.50;

        // 1. Calibrate Question Statistics
        int responseTime = Math.max(1, request.getResponseTimeSeconds());
        question.recordAttempt(isCorrect, responseTime);
        questionRepository.save(question);

        // 2. Response Time Intelligence: Guess & Careless Error Detection
        boolean possibleGuess = false;
        boolean possibleCarelessError = false;
        if (responseTime <= 3 && isCorrect && question.getDifficulty() == CourseDifficulty.ADVANCED && probBefore < 0.40) {
            possibleGuess = true;
        } else if (responseTime <= 3 && !isCorrect && question.getDifficulty() == CourseDifficulty.BEGINNER && probBefore > 0.80) {
            possibleCarelessError = true;
        }

        // 3. Update BKT State
        LearnerKnowledgeState stateAfter = bktService.updateKnowledgeState(
                user,
                session.getAssessment().getSkill(),
                concept,
                isCorrect,
                responseTime
        );
        double probAfter = stateAfter.getKnowledgeProbability();

        // 4. Update Difficulty with Smoothing
        CourseDifficulty currentDiff = session.getCurrentDifficulty();
        CourseDifficulty nextDiff = difficultyService.determineNextDifficulty(
                user.getId(),
                concept,
                currentDiff,
                isCorrect,
                responseTime,
                stateAfter.getConsecutiveCorrect(),
                stateAfter.getConsecutiveIncorrect()
        );

        // 5. Update Ability Estimate & Confidence
        int newQuestionsAsked = session.getQuestionsAsked() + 1;
        int newCorrect = isCorrect ? session.getCorrectAnswers() + 1 : session.getCorrectAnswers();
        double accuracy = (double) newCorrect / newQuestionsAsked;
        double abilityEstimate = (probAfter * 0.6) + (accuracy * 0.4);
        double sampleFactor = Math.min(1.0, (double) newQuestionsAsked / MIN_QUESTIONS);
        double confidence = Math.min(1.0, (sampleFactor * 0.5) + (accuracy * 0.3) + (probAfter * 0.2));

        session.recordAnswer(isCorrect, responseTime, nextDiff, abilityEstimate, confidence);

        // 6. Record Adaptive Response Entry
        AdaptiveAssessmentResponse response = AdaptiveAssessmentResponse.builder()
                .session(session)
                .question(question)
                .conceptName(concept)
                .difficulty(question.getDifficulty())
                .selectedAnswer(provided)
                .correct(isCorrect)
                .responseTimeSeconds(responseTime)
                .attemptNumber(newQuestionsAsked)
                .bktProbabilityBefore(probBefore)
                .bktProbabilityAfter(probAfter)
                .difficultyBefore(currentDiff)
                .difficultyAfter(nextDiff)
                .confidence(confidence)
                .possibleGuess(possibleGuess)
                .possibleCarelessError(possibleCarelessError)
                .answeredAt(Instant.now())
                .build();
        responseRepository.save(response);

        // 7. Check Stopping Conditions
        List<AdaptiveAssessmentResponse> allResponses = responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId);
        boolean shouldStop = shouldStopAssessment(session, allResponses);

        if (shouldStop) {
            session.setStatus(AdaptiveSessionStatus.COMPLETED);
            session.setCompletedAt(Instant.now());
            session.setTerminationReason(deriveTerminationReason(session, allResponses));
            sessionRepository.save(session);
            triggerPostCompletionWorkflow(session, user);
        } else {
            sessionRepository.save(session);
        }

        String feedback = isCorrect
                ? "Correct! Solid grasp of " + concept + "."
                : "Incorrect. " + concept + " will be reinforced in upcoming practice.";

        return AdaptiveAssessmentDto.AnswerSubmissionResult.builder()
                .correct(isCorrect)
                .feedback(feedback)
                .explanation("Target answer: " + expected)
                .updatedKnowledgeProbability(probAfter)
                .updatedMasteryLevel(stateAfter.getMasteryLevel().name())
                .nextDifficulty(nextDiff)
                .possibleGuess(possibleGuess)
                .possibleCarelessError(possibleCarelessError)
                .sessionComplete(shouldStop)
                .terminationReason(session.getTerminationReason())
                .build();
    }

    @Transactional
    public AdaptiveAssessmentDto.SessionResultResponse getSessionResult(UUID sessionId, User user) {
        AdaptiveAssessmentSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        List<AdaptiveAssessmentResponse> responses = responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId);

        int totalQuestions = session.getQuestionsAsked() > 0 ? session.getQuestionsAsked() : responses.size();
        double score = totalQuestions == 0 ? 0.0 :
                Math.round(((double) session.getCorrectAnswers() / totalQuestions) * 1000.0) / 10.0;


        LearnerMasteryDto.Summary masterySummary = masteryService.getMasterySummary(user.getId());
        LearnerBehaviorProfile behaviorProfile = behaviorService.getBehaviorProfile(user.getId());

        Map<String, Double> conceptCoverage = new HashMap<>();
        for (AdaptiveAssessmentResponse r : responses) {
            conceptCoverage.put(r.getConceptName(), r.getBktProbabilityAfter());
        }

        String recommendedAction = masterySummary.getWeakSkills().isEmpty()
                ? "Continue to next module in your personalized learning path."
                : "Revise foundational prerequisites for " + masterySummary.getWeakSkills().get(0) + " before advancing.";

        return AdaptiveAssessmentDto.SessionResultResponse.builder()
                .sessionId(sessionId)
                .assessmentId(session.getAssessment().getId())
                .assessmentTitle(session.getAssessment().getTitle())
                .overallScore(score)
                .masteryEstimate(Math.round(session.getCurrentAbilityEstimate() * 1000.0) / 10.0)
                .confidenceScore(session.getConfidenceScore())
                .confidenceLevel(session.getConfidenceLevel())
                .difficultyReached(session.getCurrentDifficulty())
                .questionsAnswered(session.getQuestionsAsked())
                .correctAnswers(session.getCorrectAnswers())
                .incorrectAnswers(session.getIncorrectAnswers())
                .averageResponseTimeSeconds(Math.round(session.getAverageResponseTimeSeconds() * 10.0) / 10.0)
                .strongSkills(masterySummary.getMasteredSkills())
                .developingSkills(masterySummary.getDevelopingSkills())
                .weakSkills(masterySummary.getWeakSkills())
                .revisionRequired(masterySummary.getRevisionRequiredSkills())
                .behaviorCategory(behaviorProfile.getBehaviorCategory())
                .behaviorInsights(behaviorProfile.getBehaviorInsights())
                .recommendedNextAction(recommendedAction)
                .conceptCoverage(conceptCoverage)
                .build();
    }

    @Transactional(readOnly = true)
    public AdaptiveAssessmentDto.SessionAnalyticsResponse getSessionAnalytics(UUID sessionId, User user) {
        AdaptiveAssessmentSession session = sessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        List<AdaptiveAssessmentResponse> responses = responseRepository.findBySessionIdOrderByAttemptNumberAsc(sessionId);

        List<Boolean> accuracyTrend = responses.stream().map(AdaptiveAssessmentResponse::isCorrect).toList();
        List<String> diffTrend = responses.stream().map(r -> r.getDifficulty().name()).toList();
        List<Integer> timeTrend = responses.stream().map(AdaptiveAssessmentResponse::getResponseTimeSeconds).toList();

        Map<String, Double> deltas = new HashMap<>();
        for (AdaptiveAssessmentResponse r : responses) {
            deltas.put(r.getConceptName(), Math.round((r.getBktProbabilityAfter() - r.getBktProbabilityBefore()) * 100.0) / 100.0);
        }

        int totalTime = responses.stream().mapToInt(AdaptiveAssessmentResponse::getResponseTimeSeconds).sum();

        return AdaptiveAssessmentDto.SessionAnalyticsResponse.builder()
                .sessionId(sessionId)
                .accuracyTrend(accuracyTrend)
                .difficultyProgression(diffTrend)
                .responseTimeTrend(timeTrend)
                .conceptMasteryDeltas(deltas)
                .totalTimeSeconds(totalTime)
                .consistencyRating(session.getConfidenceLevel() == ConfidenceLevel.HIGH ? "HIGH_CONFIDENCE" : "MODERATE")
                .build();
    }

    private AssessmentQuestion selectBestCatQuestion(
            List<AssessmentQuestion> availableQuestions,
            AdaptiveAssessmentSession session,
            User user
    ) {
        CourseDifficulty targetDifficulty = session.getCurrentDifficulty();

        // 1. Try questions with matching target difficulty
        List<AssessmentQuestion> matchedDifficulty = availableQuestions.stream()
                .filter(q -> q.getDifficulty() == targetDifficulty)
                .toList();

        if (!matchedDifficulty.isEmpty()) {
            return matchedDifficulty.get(0);
        }

        log.warn("[CAT Question Selection] Question bank lacks available {} question for assessment={}. Attempting closest difficulty fallback.",
                targetDifficulty, session.getAssessment().getId());

        // 2. Fallback to closest valid difficulty question in preference hierarchy
        List<CourseDifficulty> fallbackHierarchy = getDifficultyFallbackOrder(targetDifficulty);
        for (CourseDifficulty fallbackDiff : fallbackHierarchy) {
            List<AssessmentQuestion> fallbackMatches = availableQuestions.stream()
                    .filter(q -> q.getDifficulty() == fallbackDiff)
                    .toList();
            if (!fallbackMatches.isEmpty()) {
                log.info("[CAT Question Selection] Falling back from {} to closest available difficulty: {}",
                        targetDifficulty, fallbackDiff);
                return fallbackMatches.get(0);
            }
        }

        // 3. Last resort from remaining available question pool
        return availableQuestions.get(0);
    }

    private List<CourseDifficulty> getDifficultyFallbackOrder(CourseDifficulty target) {
        if (target == null) {
            return List.of(CourseDifficulty.BEGINNER, CourseDifficulty.INTERMEDIATE, CourseDifficulty.ADVANCED);
        }
        return switch (target) {
            case ADVANCED, HIGH -> List.of(CourseDifficulty.INTERMEDIATE, CourseDifficulty.BEGINNER);
            case INTERMEDIATE, MEDIUM -> List.of(CourseDifficulty.BEGINNER, CourseDifficulty.ADVANCED);
            case BEGINNER, EASY, ALL_LEVELS -> List.of(CourseDifficulty.INTERMEDIATE, CourseDifficulty.ADVANCED);
            default -> List.of(CourseDifficulty.BEGINNER, CourseDifficulty.INTERMEDIATE, CourseDifficulty.ADVANCED);
        };
    }

    private boolean shouldStopAssessment(AdaptiveAssessmentSession session, List<AdaptiveAssessmentResponse> responses) {
        if (responses.size() >= MAX_QUESTIONS) {
            return true;
        }

        if (responses.size() >= MIN_QUESTIONS) {
            // Check confidence & stabilization
            if (session.getConfidenceScore() >= 0.85) {
                return true;
            }

            // Check if last 3 responses had stable mastery (change < 0.05)
            if (responses.size() >= 5) {
                int lastIdx = responses.size() - 1;
                double delta1 = Math.abs(responses.get(lastIdx).getBktProbabilityAfter() - responses.get(lastIdx).getBktProbabilityBefore());
                double delta2 = Math.abs(responses.get(lastIdx - 1).getBktProbabilityAfter() - responses.get(lastIdx - 1).getBktProbabilityBefore());
                double delta3 = Math.abs(responses.get(lastIdx - 2).getBktProbabilityAfter() - responses.get(lastIdx - 2).getBktProbabilityBefore());

                if (delta1 < 0.05 && delta2 < 0.05 && delta3 < 0.05) {
                    return true;
                }
            }
        }

        return false;
    }

    private String deriveTerminationReason(AdaptiveAssessmentSession session, List<AdaptiveAssessmentResponse> responses) {
        if (responses.size() >= MAX_QUESTIONS) {
            return "Maximum assessment question ceiling reached.";
        }
        if (session.getConfidenceScore() >= 0.85) {
            return "High measurement confidence achieved across target competencies.";
        }
        return "Knowledge state estimate stabilized with minimal variance.";
    }

    private void triggerPostCompletionWorkflow(AdaptiveAssessmentSession session, User user) {
        try {
            double finalScore = session.getQuestionsAsked() > 0
                    ? ((double) session.getCorrectAnswers() / session.getQuestionsAsked()) * 100.0
                    : 70.0;

            String reason = String.format("Adaptive Assessment '%s' completed with score %.1f%%",
                    session.getAssessment().getTitle(), finalScore);

            // Trigger Learning Path Engine Recalculation
            recalculationService.triggerRecalculation(user.getId(), reason);

            // Fire Notification
            notificationService.createNotification(
                    user.getId(),
                    "Assessment Completed: " + session.getAssessment().getTitle(),
                    String.format("You scored %.0f%% (%d/%d correct). Your learning path and mastery states have been updated.",
                            finalScore, session.getCorrectAnswers(), session.getQuestionsAsked()),
                    NotificationCategory.ASSESSMENTS,
                    "/assessments"
            );
        } catch (Exception e) {
            log.warn("[AdaptiveAssessmentService] Post-completion trigger error: {}", e.getMessage());
        }
    }

    // ==========================================
    // Backward Compatibility Methods
    // ==========================================
    @Transactional(readOnly = true)
    public AdaptiveAssessmentDto.QuestionResponse getAdaptiveQuestion(UUID assessmentId, User user) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        String skillName = assessment.getSkill() != null ? assessment.getSkill().getName() : "General";
        CourseDifficulty targetDifficulty = difficultyService.determineDifficulty(
                user != null ? user.getId() : null,
                skillName,
                CourseDifficulty.BEGINNER
        );

        List<AssessmentQuestion> matchingQuestions = questionRepository.findAllByAssessmentIdAndDifficulty(assessmentId, targetDifficulty);
        if (matchingQuestions.isEmpty()) {
            matchingQuestions = questionRepository.findAllByAssessmentId(assessmentId);
        }

        if (matchingQuestions.isEmpty()) {
            throw new IllegalStateException("No questions configured for assessment: " + assessmentId);
        }

        AssessmentQuestion question = matchingQuestions.get(0);

        List<String> options = List.of();
        if (question.getOptionsJson() != null && !question.getOptionsJson().isBlank()) {
            try {
                options = objectMapper.readValue(question.getOptionsJson(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse options JSON for question {}: {}", question.getId(), e.getMessage());
            }
        }

        return AdaptiveAssessmentDto.QuestionResponse.builder()
                .questionId(question.getId().toString())
                .assessmentId(assessment.getId().toString())
                .assessmentTitle(assessment.getTitle())
                .skillName(skillName)
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType() != null ? question.getQuestionType().name() : "SINGLE_CHOICE")
                .options(options)
                .difficulty(question.getDifficulty())
                .questionNumber(1)
                .totalQuestions(matchingQuestions.size())
                .build();
    }

    @Transactional
    public AdaptiveAssessmentDto.AnswerResult processAdaptiveAnswer(
            UUID assessmentId,
            AdaptiveAssessmentDto.AnswerRequest request,
            User user
    ) {
        UUID questionId = UUID.fromString(request.getQuestionId());
        AssessmentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElse(question.getAssessment());

        String expected = question.getCorrectAnswer() != null ? question.getCorrectAnswer().trim() : "";
        String provided = request.getAnswer() != null ? request.getAnswer().trim() : "";
        boolean isCorrect = expected.equalsIgnoreCase(provided);

        question.recordAttempt(isCorrect, Math.max(1, request.getResponseTimeSeconds()));
        questionRepository.save(question);

        String concept = assessment != null && assessment.getSkill() != null ? assessment.getSkill().getName() : "General";
        LearnerKnowledgeState state = bktService.updateKnowledgeState(
                user,
                assessment != null ? assessment.getSkill() : null,
                concept,
                isCorrect,
                request.getResponseTimeSeconds()
        );

        CourseDifficulty nextDiff = difficultyService.determineDifficulty(user.getId(), concept, CourseDifficulty.BEGINNER);

        String feedback = isCorrect
                ? "Correct! You demonstrated clear understanding of " + concept + "."
                : "Not quite. " + concept + " will be reinforced in upcoming practice sessions.";

        return AdaptiveAssessmentDto.AnswerResult.builder()
                .correct(isCorrect)
                .feedback(feedback)
                .updatedKnowledgeProbability(state.getKnowledgeProbability())
                .updatedMasteryLevel(state.getMasteryLevel())
                .nextRecommendedDifficulty(nextDiff)
                .revisionSuggested(state.isRevisionRequired())
                .explanation("Expected answer: " + expected)
                .build();
    }
}

