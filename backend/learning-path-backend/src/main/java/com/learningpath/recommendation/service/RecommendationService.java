package com.learningpath.recommendation.service;

import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.recommendation.client.MlRecommendationClient;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.engine.RecommendationScoringEngine;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.ai.reasoning.dto.CandidateCourseDto;
import com.learningpath.ai.reasoning.dto.GeminiCourseExplanationDto;
import com.learningpath.ai.reasoning.dto.GeminiReasoningInput;
import com.learningpath.ai.reasoning.dto.GeminiReasoningResult;
import com.learningpath.ai.reasoning.dto.LearnerProfileDto;
import com.learningpath.ai.reasoning.service.GeminiReasoningService;
import com.learningpath.repository.CourseRepository;
import com.learningpath.skilldependency.service.SkillDependencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final UserRepository userRepository;
    private final SkillGapService skillGapService;
    private final CourseRepository courseRepository;
    private final CourseSkillRepository courseSkillRepository;
    private final MlRecommendationClient mlRecommendationClient;
    private final GeminiReasoningService geminiReasoningService;
    private final SkillDependencyService skillDependencyService;

    @Value("${recommendation.scoring.rule-weight:0.70}")
    private double ruleWeight = 0.70;

    @Value("${recommendation.scoring.ml-weight:0.30}")
    private double mlWeight = 0.30;

    private final RecommendationScoringEngine scoringEngine = new RecommendationScoringEngine();

    @Transactional(readOnly = true)
    public RecommendationSummaryResponse getRecommendationsForUser(UUID userId, UUID careerId) {
        return getRecommendationsForUser(userId, careerId, 10, false, null);
    }

    @Transactional(readOnly = true)
    public RecommendationSummaryResponse getRecommendationsForUser(
            UUID userId,
            UUID careerId,
            Integer limit,
            Boolean freeOnly,
            CourseDifficulty difficulty
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 1. Calculate user's skill gaps against career target
        SkillGapAnalysisResponse skillGaps = skillGapService.analyzeSkillGap(userId, careerId);

        Map<String, SkillGapItemResponse> gapMap = skillGaps.gaps().stream()
                .collect(Collectors.toMap(SkillGapItemResponse::skillName, Function.identity(), (a, b) -> a));

        Set<UUID> gapSkillIds = skillGaps.gaps().stream()
                .filter(g -> g.gapType() != GapType.NO_GAP)
                .map(SkillGapItemResponse::skillId)
                .collect(Collectors.toSet());

        // 2. Candidate Course Generation
        List<CourseSkill> candidateCourseSkills;
        if (!gapSkillIds.isEmpty()) {
            candidateCourseSkills = courseSkillRepository.findBySkillIdIn(gapSkillIds);
        } else {
            // Fallback: load all courses if learner has 0 skill gaps
            candidateCourseSkills = courseSkillRepository.findAll();
        }

        // Group CourseSkill mappings by Course ID
        Map<UUID, List<CourseSkill>> courseSkillGrouped = candidateCourseSkills.stream()
                .collect(Collectors.groupingBy(cs -> cs.getCourse().getId()));

        List<CourseRecommendationResponse> scoredRecommendations = new ArrayList<>();

        // 3. Feature Generation, ML Prediction Call, & Scoring
        for (Map.Entry<UUID, List<CourseSkill>> entry : courseSkillGrouped.entrySet()) {
            List<CourseSkill> skillsForCourse = entry.getValue();
            if (skillsForCourse.isEmpty()) continue;

            Course course = skillsForCourse.get(0).getCourse();

            // Filter by freeOnly if requested
            if (Boolean.TRUE.equals(freeOnly) && !course.isFree()) {
                continue;
            }

            // Filter by difficulty if requested
            if (difficulty != null && course.getDifficulty() != CourseDifficulty.ALL_LEVELS && course.getDifficulty() != difficulty) {
                continue;
            }

            // Build dynamic 10-feature request for Python ML microservice
            MlPredictionRequest mlRequest = scoringEngine.buildMlPredictionRequest(course, skillsForCourse, gapMap, user);

            // Execute ML service prediction with automatic fallback error handling
            Optional<MlPredictionResponse> mlResponseOpt = mlRecommendationClient.predict(mlRequest);
            Double mlScore = mlResponseOpt.map(MlPredictionResponse::recommendationScore).orElse(null);

            // Score candidate course (Configurable Rule + ML hybrid scoring)
            CourseRecommendationResponse scored = scoringEngine.scoreAndBuildRecommendation(
                    0, course, skillsForCourse, gapMap, user, mlScore, ruleWeight, mlWeight
            );

            scoredRecommendations.add(scored);
        }

        // 4. Rank Recommendations by Final Score Descending
        scoredRecommendations.sort(Comparator.comparingDouble(CourseRecommendationResponse::finalScore).reversed());

        int effectiveLimit = (limit != null && limit > 0) ? Math.min(limit, scoredRecommendations.size()) : scoredRecommendations.size();

        // Assign Rank indices (1, 2, 3...)
        List<CourseRecommendationResponse> rankedRecommendations = new ArrayList<>();
        for (int i = 0; i < effectiveLimit; i++) {
            CourseRecommendationResponse item = scoredRecommendations.get(i);
            rankedRecommendations.add(new CourseRecommendationResponse(
                    i + 1,
                    item.courseId(),
                    item.courseTitle(),
                    item.provider(),
                    item.url(),
                    item.difficulty(),
                    item.courseType(),
                    item.rating(),
                    item.price(),
                    item.isFree(),
                    item.ruleBasedScore(),
                    item.mlScore(),
                    item.finalScore(),
                    item.matchedSkills(),
                    item.gapSkillsAddressed(),
                    item.explanation()
            ));
        }

        // 5. Enhance Recommendations with Gemini AI Reasoning & Explanations
        List<CandidateCourseDto> candidateDtos = new ArrayList<>();
        for (CourseRecommendationResponse r : rankedRecommendations) {
            courseRepository.findById(r.courseId()).ifPresent(c -> {
                candidateDtos.add(new CandidateCourseDto(
                        r.courseId(),
                        c.getCourseCode(),
                        r.courseTitle(),
                        r.provider(),
                        r.difficulty() != null ? r.difficulty().name() : "BEGINNER",
                        r.courseType() != null ? r.courseType().name() : "VIDEO_COURSE",
                        r.matchedSkills(),
                        r.gapSkillsAddressed(),
                        r.ruleBasedScore() != null ? r.ruleBasedScore() : 0.0,
                        r.mlScore(),
                        r.finalScore()
                ));
            });
        }

        LearnerProfileDto learnerProfile = new LearnerProfileDto(
                skillGaps.careerName(),
                user.getExperienceLevel() != null ? user.getExperienceLevel().name() : "BEGINNER",
                user.getDailyLearningHours() != null ? user.getDailyLearningHours().doubleValue() : 2.0,
                user.getLearningStyle() != null ? user.getLearningStyle().name() : "VISUAL",
                user.getPreferredContentType() != null ? user.getPreferredContentType().name() : "ARTICLE"
        );

        List<String> allGapSkillNames = skillGaps.gaps().stream()
                .filter(g -> g.gapType() != GapType.NO_GAP)
                .map(SkillGapItemResponse::skillName)
                .toList();

        List<String> prereqOrder = skillDependencyService.getLearningOrder(allGapSkillNames).learningOrder();

        GeminiReasoningInput reasoningInput = new GeminiReasoningInput(
                learnerProfile,
                skillGaps.gaps(),
                candidateDtos,
                prereqOrder
        );

        GeminiReasoningResult reasoningResult = geminiReasoningService.generateReasoning(reasoningInput);

        Map<UUID, String> reasonMap = new HashMap<>();
        if (reasoningResult != null && reasoningResult.recommendations() != null) {
            for (GeminiCourseExplanationDto exp : reasoningResult.recommendations()) {
                if (exp.courseId() != null && exp.reason() != null) {
                    reasonMap.put(exp.courseId(), exp.reason());
                }
            }
        }

        List<CourseRecommendationResponse> enhancedRecommendations = new ArrayList<>();
        for (CourseRecommendationResponse item : rankedRecommendations) {
            String enhancedExplanation = reasonMap.getOrDefault(item.courseId(), item.explanation());
            enhancedRecommendations.add(new CourseRecommendationResponse(
                    item.rank(),
                    item.courseId(),
                    item.courseTitle(),
                    item.provider(),
                    item.url(),
                    item.difficulty(),
                    item.courseType(),
                    item.rating(),
                    item.price(),
                    item.isFree(),
                    item.ruleBasedScore(),
                    item.mlScore(),
                    item.finalScore(),
                    item.matchedSkills(),
                    item.gapSkillsAddressed(),
                    enhancedExplanation
            ));
        }

        // Summary Statistics
        int totalCandidates = scoredRecommendations.size();
        boolean hasGaps = !gapSkillIds.isEmpty();

        return new RecommendationSummaryResponse(
                userId,
                user.getFullName(),
                careerId,
                skillGaps.careerName(),
                hasGaps,
                totalCandidates,
                enhancedRecommendations
        );
    }
}
