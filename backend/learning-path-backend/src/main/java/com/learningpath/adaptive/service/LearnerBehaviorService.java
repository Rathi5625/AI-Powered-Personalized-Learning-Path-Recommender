package com.learningpath.adaptive.service;

import com.learningpath.adaptive.dto.LearnerBehaviorProfile;
import com.learningpath.entity.*;
import com.learningpath.entity.enums.LearnerBehaviorCategory;
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
public class LearnerBehaviorService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final AssessmentResultRepository assessmentResultRepository;
    private final LearnerKnowledgeStateRepository knowledgeStateRepository;
    private final AdaptiveAssessmentSessionRepository sessionRepository;
    private final AdaptiveAssessmentResponseRepository responseRepository;

    @Transactional(readOnly = true)
    public LearnerBehaviorProfile getBehaviorProfile(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return LearnerBehaviorProfile.builder()
                    .insufficientData(true)
                    .dataQualityStatus("INSUFFICIENT_DATA")
                    .behaviorCategory(LearnerBehaviorCategory.INSUFFICIENT_DATA.name())
                    .behaviorInsights(List.of("No learner profile data available."))
                    .build();
        }

        List<UserProgress> progresses = userProgressRepository.findByUserId(userId);
        List<AssessmentResult> results = assessmentResultRepository.findAllByUserIdOrderByCompletedAtDesc(userId);
        List<LearnerKnowledgeState> knowledgeStates = knowledgeStateRepository.findByUserId(userId);
        List<AdaptiveAssessmentSession> sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        List<AdaptiveAssessmentResponse> responses = responseRepository.findBySession_UserIdOrderByAnsweredAtDesc(userId);

        int totalInteractions = progresses.size() + results.size() + knowledgeStates.size() + responses.size();

        if (totalInteractions < 3) {
            return LearnerBehaviorProfile.builder()
                    .preferredDifficulty(user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "BEGINNER")
                    .learningVelocity(0.50)
                    .consistency(0.60)
                    .assessmentAccuracy(0.70)
                    .revisionNeed(0.20)
                    .preferredSessionLengthMinutes(45)
                    .strongestLearningFormat(user.getPreferredContentType() != null ? user.getPreferredContentType().name() : "VIDEO")
                    .activeStreakDays(7)
                    .totalSessionsRecorded(totalInteractions)
                    .insufficientData(true)
                    .dataQualityStatus("INSUFFICIENT_DATA")
                    .behaviorCategory(LearnerBehaviorCategory.INSUFFICIENT_DATA.name())
                    .behaviorInsights(List.of("Complete 3 assessments or study modules to unlock behavior analytics."))
                    .build();
        }

        // 1. Assessment accuracy
        double accuracy;
        if (!responses.isEmpty()) {
            long correctCount = responses.stream().filter(AdaptiveAssessmentResponse::isCorrect).count();
            accuracy = (double) correctCount / responses.size();
        } else if (!results.isEmpty()) {
            accuracy = results.stream().mapToDouble(r -> r.getScore() != null ? r.getScore().doubleValue() : 0.0).average().orElse(70.0) / 100.0;
        } else {
            accuracy = 0.70;
        }

        // 2. Average Response Time
        double avgResponseTime = responses.stream()
                .mapToInt(AdaptiveAssessmentResponse::getResponseTimeSeconds)
                .average()
                .orElse(35.0);

        // 3. Revision Need
        long revisionCount = knowledgeStates.stream().filter(LearnerKnowledgeState::isRevisionRequired).count();
        double revisionNeed = knowledgeStates.isEmpty() ? 0.20 : (double) revisionCount / knowledgeStates.size();

        // 4. Learning velocity & consistency
        long completedCourses = progresses.stream()
                .filter(p -> p.getCompletionPercentage() != null && p.getCompletionPercentage().doubleValue() >= 100.0)
                .count();
        double velocity = Math.min(1.0, 0.40 + (completedCourses * 0.15));
        double consistency = Math.min(1.0, 0.60 + (sessions.size() * 0.05));

        // 5. Average Knowledge State
        double avgMastery = knowledgeStates.stream()
                .mapToDouble(LearnerKnowledgeState::getKnowledgeProbability)
                .average()
                .orElse(0.50);

        // 6. Determine Learner Behavioral Category
        LearnerBehaviorCategory category;
        List<String> insights = new ArrayList<>();

        if (avgMastery >= 0.82) {
            category = LearnerBehaviorCategory.HIGH_MASTERY;
            insights.add("Consistently achieves strong concept mastery across domains.");
        } else if (accuracy >= 0.80 && avgResponseTime < 25.0) {
            category = LearnerBehaviorCategory.FAST_ACCURATE;
            insights.add("High problem-solving speed with strong accuracy.");
        } else if (accuracy >= 0.75 && avgResponseTime > 50.0) {
            category = LearnerBehaviorCategory.CAUTIOUS;
            insights.add("Takes deliberate, careful time before answering, resulting in solid accuracy.");
        } else if (accuracy < 0.50 || revisionNeed > 0.40) {
            category = LearnerBehaviorCategory.STRUGGLING;
            insights.add("Currently encountering knowledge gaps; prerequisite reinforcement recommended.");
        } else if (Math.abs(accuracy - 0.50) < 0.15 && responses.size() > 5) {
            category = LearnerBehaviorCategory.INCONSISTENT;
            insights.add("Performance varies between difficulty tiers; building core fluency will stabilize scores.");
        } else {
            category = LearnerBehaviorCategory.STEADY_LEARNER;
            insights.add("Steady, disciplined progression with balanced pace and accuracy.");
        }

        return LearnerBehaviorProfile.builder()
                .preferredDifficulty(user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "INTERMEDIATE")
                .learningVelocity(Math.round(velocity * 100.0) / 100.0)
                .consistency(Math.round(consistency * 100.0) / 100.0)
                .assessmentAccuracy(Math.round(accuracy * 100.0) / 100.0)
                .revisionNeed(Math.round(revisionNeed * 100.0) / 100.0)
                .preferredSessionLengthMinutes(45)
                .strongestLearningFormat(user.getPreferredContentType() != null ? user.getPreferredContentType().name() : "VIDEO")
                .activeStreakDays(7)
                .totalSessionsRecorded(totalInteractions)
                .insufficientData(false)
                .dataQualityStatus(totalInteractions >= 5 ? "COMPLETE" : "PARTIAL")
                .behaviorCategory(category.name())
                .behaviorInsights(insights)
                .build();
    }
}
