package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.Career;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.learningpath.dto.ActiveLearningPathResponse;
import com.learningpath.learningpath.service.LearningPathPersistenceService;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.recommendation.service.SkillGapService;
import com.learningpath.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Service orchestrating aggregated data for the learner's dashboard.
 * Reuses existing domain services without duplicating business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserService userService;
    private final LearningPathPersistenceService learningPathPersistenceService;
    private final UserProgressService userProgressService;
    private final SkillGapService skillGapService;
    private final RecommendationService recommendationService;
    private final CareerRepository careerRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UUID userId) {
        log.info("[DashboardService] Loading aggregated dashboard for userId={}", userId);

        // 1. User Profile (throws ResourceNotFoundException if user doesn't exist)
        UserResponse user = userService.getUserById(userId);

        // 2. Active Learning Path (null if none active)
        ActiveLearningPathResponse activePath = null;
        try {
            activePath = learningPathPersistenceService.getActivePath(userId);
        } catch (ResourceNotFoundException e) {
            log.debug("[DashboardService] No active learning path for userId={}", userId);
        }

        // 3. Progress Summary
        UserProgressSummaryResponse progressSummary = userProgressService.getProgressSummary(userId);

        // 4 & 5. Skill Gap Summary & Top Recommendations
        SkillGapSummaryResponse skillGapSummary = null;
        List<CourseRecommendationResponse> topRecommendations = Collections.emptyList();

        String careerGoal = user.careerGoal();
        if (careerGoal != null && !careerGoal.trim().isEmpty()) {
            Career targetCareer = careerRepository.findByTitle(careerGoal.trim())
                    .orElseGet(() -> {
                        List<Career> matches = careerRepository.findByTitleContainingIgnoreCase(careerGoal.trim());
                        return matches.isEmpty() ? null : matches.get(0);
                    });

            if (targetCareer != null) {
                try {
                    SkillGapAnalysisResponse gapAnalysis = skillGapService.analyzeSkillGap(userId, targetCareer.getId());
                    double matchPercentage = gapAnalysis.totalRequiredSkills() > 0
                            ? Math.round(((double) gapAnalysis.skillsWithNoGap() / gapAnalysis.totalRequiredSkills()) * 1000.0) / 10.0
                            : 100.0;

                    skillGapSummary = new SkillGapSummaryResponse(
                            gapAnalysis.totalRequiredSkills(),
                            gapAnalysis.skillsWithNoGap(),
                            gapAnalysis.partialGaps(),
                            gapAnalysis.fullGaps(),
                            matchPercentage
                    );

                    RecommendationSummaryResponse recSummary = recommendationService.getRecommendationsForUser(
                            userId, targetCareer.getId(), 5, false, null
                    );
                    if (recSummary != null && recSummary.recommendations() != null) {
                        topRecommendations = recSummary.recommendations();
                    }
                } catch (Exception e) {
                    log.warn("[DashboardService] Could not calculate skill gaps or recommendations for userId={}: {}", userId, e.getMessage());
                }
            }
        }

        return new DashboardResponse(
                user,
                activePath,
                progressSummary,
                skillGapSummary,
                topRecommendations
        );
    }
}
