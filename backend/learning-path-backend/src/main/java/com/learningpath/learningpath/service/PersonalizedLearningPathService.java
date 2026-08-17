package com.learningpath.learningpath.service;

import com.learningpath.ai.dto.AiContext;
import com.learningpath.ai.dto.AiOperation;
import com.learningpath.ai.dto.AiRequest;
import com.learningpath.ai.dto.AiResponse;
import com.learningpath.ai.service.AiService;
import com.learningpath.ai.validation.LearningPathValidator;
import com.learningpath.ai.validation.ValidationResult;
import com.learningpath.entity.Career;
import com.learningpath.entity.User;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.learningpath.dto.*;
import com.learningpath.recommendation.dto.CourseRecommendationResponse;
import com.learningpath.recommendation.dto.RecommendationSummaryResponse;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.service.RecommendationService;
import com.learningpath.recommendation.service.SkillGapService;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.UserRepository;
import com.learningpath.repository.UserSkillRepository;
import com.learningpath.skilldependency.dto.LearningOrderResponse;
import com.learningpath.skilldependency.service.SkillDependencyService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalizedLearningPathService {

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillGapService skillGapService;
    private final SkillDependencyService dependencyService;
    private final RecommendationService recommendationService;
    private final LearningPathPromptBuilder promptBuilder;
    private final AiService aiService;
    private final LearningPathValidator validator;
    private final ObjectMapper objectMapper;
    private final LearningPathPersistenceService persistenceService;

    public PersonalizedLearningPathResponse generateLearningPath(UUID userId, UUID careerId) {
        log.info("[PersonalizedLearningPathService] Generating learning path for userId: {}, careerId: {}", userId, careerId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Career career = (careerId != null) ? careerRepository.findById(careerId).orElse(null) : null;
        String targetCareer = (career != null) ? career.getTitle() : user.getTargetCareer();
        if (targetCareer == null || targetCareer.trim().isEmpty()) {
            targetCareer = "Software Engineering";
        }

        // Step 1: Identify current skills & skill gaps
        List<String> currentSkills = userSkillRepository.findByUserId(userId).stream()
                .map(us -> us.getSkill().getName())
                .distinct()
                .toList();

        SkillGapAnalysisResponse gapAnalysis = skillGapService.analyzeSkillGap(userId, careerId);
        List<String> skillGaps = gapAnalysis.gaps().stream()
                .map(SkillGapItemResponse::skillName)
                .distinct()
                .toList();

        if (skillGaps.isEmpty()) {
            skillGaps = List.of("Java", "Spring Boot", "REST APIs", "SQL Databases");
        }

        // Step 2: Determine prerequisite topological learning order
        LearningOrderResponse orderResponse = dependencyService.getLearningOrder(skillGaps);
        List<String> orderedTargetSkills = orderResponse.learningOrder();
        if (orderedTargetSkills.isEmpty()) {
            orderedTargetSkills = skillGaps;
        }

        // Step 3: Fetch candidate course recommendations
        RecommendationSummaryResponse recSummary = recommendationService.getRecommendationsForUser(userId, careerId);
        List<CourseRecommendationResponse> recs = (recSummary != null && recSummary.recommendations() != null)
                ? recSummary.recommendations()
                : Collections.emptyList();

        List<RecommendedCourseItem> candidateCourses = recs.stream()
                .map(r -> new RecommendedCourseItem(
                        r.courseId(),
                        r.courseTitle(),
                        r.provider(),
                        r.finalScore(),
                        (r.difficulty() != null ? r.difficulty().name() : "BEGINNER"),
                        r.matchedSkills()
                ))
                .collect(Collectors.toList());

        // Step 4: Build LearningPathContext & Pre-Validation
        String expLevelStr = (user.getExperienceLevel() != null) ? user.getExperienceLevel().name() : "BEGINNER";
        String styleStr = (user.getLearningStyle() != null) ? user.getLearningStyle().name() : "VISUAL";
        Double hours = (user.getDailyLearningHours() != null) ? user.getDailyLearningHours().doubleValue() : 2.0;

        LearningPathContext context = new LearningPathContext(
                userId,
                user.getFullName(),
                targetCareer,
                expLevelStr,
                styleStr,
                hours,
                currentSkills,
                skillGaps,
                orderedTargetSkills,
                candidateCourses
        );

        ValidationResult contextVal = validator.validateContext(context);
        if (!contextVal.valid()) {
            log.warn("[PersonalizedLearningPathService] Pre-validation failed for context: {}", contextVal.errors());
            PersonalizedLearningPathResponse fallback = generateRuleBasedFallback(context);
            persistenceService.saveLearningPath(userId, careerId, fallback);
            return fallback;
        }

        // Step 5: Attempt Gemini AI Generation with AT MOST 1 Retry
        PersonalizedLearningPathResponse finalResponse = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                String promptText = promptBuilder.buildPrompt(context);
                AiContext aiContext = new AiContext(
                        userId,
                        careerId,
                        targetCareer,
                        user.getTargetCareer(),
                        styleStr,
                        hours,
                        currentSkills,
                        skillGaps,
                        candidateCourses.stream().map(RecommendedCourseItem::courseTitle).toList()
                );

                AiRequest aiRequest = new AiRequest(AiOperation.LEARNING_PATH, aiContext);
                AiResponse aiResponse = aiService.executeOperation(aiRequest);

                if (aiResponse.success() && aiResponse.content() != null) {
                    PersonalizedLearningPathResponse parsed = parseGeminiResponse(aiResponse, context);
                    if (parsed != null) {
                        ValidationResult valResult = validator.validateResponse(parsed, context);
                        if (valResult.valid()) {
                            log.info("[PersonalizedLearningPathService] Successfully validated Gemini learning path on attempt {}.", attempt);
                            finalResponse = parsed;
                            break;
                        } else {
                            log.warn("[PersonalizedLearningPathService] Attempt {} AI validation failed: {}", attempt, valResult.errors());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[PersonalizedLearningPathService] Exception during Gemini generation attempt {}: {}", attempt, e.getMessage());
            }
        }

        // Step 6: Fallback Deterministic Rule-Based Path if AI generation failed validation
        if (finalResponse == null) {
            log.warn("[PersonalizedLearningPathService] All AI generation attempts failed validation. Triggering rule-based fallback.");
            finalResponse = generateRuleBasedFallback(context);
        }

        // Step 7: Persist the validated path as ACTIVE in database
        try {
            persistenceService.saveLearningPath(userId, careerId, finalResponse);
        } catch (Exception e) {
            log.error("[PersonalizedLearningPathService] Failed to persist learning path for userId={}: {}", userId, e.getMessage());
        }

        return finalResponse;
    }

    private PersonalizedLearningPathResponse parseGeminiResponse(AiResponse aiResponse, LearningPathContext context) {
        try {
            String content = aiResponse.content().trim();
            int jsonStart = content.indexOf('{');
            int jsonEnd = content.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                content = content.substring(jsonStart, jsonEnd + 1);
            }

            JsonNode root = objectMapper.readTree(content);
            String summary = root.has("summary") ? root.get("summary").asText() : "Personalized Learning Path";
            JsonNode phasesNode = root.get("phases");

            if (phasesNode == null || !phasesNode.isArray()) {
                return null;
            }

            Map<UUID, RecommendedCourseItem> candidateMap = context.candidateCourses().stream()
                    .collect(Collectors.toMap(RecommendedCourseItem::courseId, c -> c, (a, b) -> a));

            List<LearningPathPhase> phases = new ArrayList<>();
            for (JsonNode phaseNode : phasesNode) {
                int phaseNum = phaseNode.has("phaseNumber") ? phaseNode.get("phaseNumber").asInt() : phases.size() + 1;
                String phaseTitle = phaseNode.has("phaseTitle") ? phaseNode.get("phaseTitle").asText() : "Phase " + phaseNum;
                String estDuration = phaseNode.has("estimatedDuration") ? phaseNode.get("estimatedDuration").asText() : "2 weeks";
                String explanation = phaseNode.has("explanation") ? phaseNode.get("explanation").asText() : "Prerequisite milestone";

                List<String> targetSkills = new ArrayList<>();
                if (phaseNode.has("targetSkills") && phaseNode.get("targetSkills").isArray()) {
                    phaseNode.get("targetSkills").forEach(s -> targetSkills.add(s.asText()));
                }

                List<RecommendedCourseItem> phaseCourses = new ArrayList<>();
                if (phaseNode.has("courseIds") && phaseNode.get("courseIds").isArray()) {
                    for (JsonNode idNode : phaseNode.get("courseIds")) {
                        try {
                            UUID courseId = UUID.fromString(idNode.asText());
                            if (candidateMap.containsKey(courseId)) {
                                phaseCourses.add(candidateMap.get(courseId));
                            } else {
                                // Add ungrounded item to trigger validator grounding rejection
                                phaseCourses.add(new RecommendedCourseItem(courseId, "Ungrounded Course", "Unknown", 0.0, "UNKNOWN", List.of()));
                            }
                        } catch (IllegalArgumentException e) {
                            phaseCourses.add(new RecommendedCourseItem(UUID.randomUUID(), "Malformed Course ID", "Unknown", 0.0, "UNKNOWN", List.of()));
                        }
                    }
                }

                phases.add(new LearningPathPhase(phaseNum, phaseTitle, targetSkills, phaseCourses, estDuration, explanation));
            }

            return PersonalizedLearningPathResponse.ok(
                    context.userId(),
                    context.targetCareer(),
                    summary,
                    phases,
                    "GEMINI",
                    aiResponse.model()
            );

        } catch (Exception e) {
            log.warn("[PersonalizedLearningPathService] Failed to parse Gemini response JSON: {}", e.getMessage());
            return null;
        }
    }

    public PersonalizedLearningPathResponse generateRuleBasedFallback(LearningPathContext context) {
        log.info("[PersonalizedLearningPathService] Generating deterministic rule-based fallback learning path...");

        List<LearningPathPhase> phases = new ArrayList<>();
        List<String> skills = context.orderedTargetSkills();
        List<RecommendedCourseItem> courses = context.candidateCourses();

        int phaseSize = Math.max(1, (int) Math.ceil(skills.size() / 2.0));
        int phaseCount = (skills.isEmpty()) ? 1 : (int) Math.ceil((double) skills.size() / phaseSize);

        for (int i = 0; i < phaseCount; i++) {
            int fromIdx = i * phaseSize;
            int toIdx = Math.min(fromIdx + phaseSize, skills.size());
            List<String> phaseSkills = (fromIdx < skills.size()) ? skills.subList(fromIdx, toIdx) : List.of("Core Skills");

            int courseFrom = i * 2;
            int courseTo = Math.min(courseFrom + 2, courses.size());
            List<RecommendedCourseItem> phaseCourses = (courseFrom < courses.size()) ? courses.subList(courseFrom, courseTo) : courses;

            String title = (i == 0) ? "Phase 1: Fundamental Prerequisites" : "Phase " + (i + 1) + ": Advanced Mastery";
            String duration = String.format("%d weeks", Math.max(1, (int)(phaseSkills.size() * 1.5)));
            String explanation = "Topological prerequisite milestone covering " + String.join(", ", phaseSkills);

            phases.add(new LearningPathPhase(i + 1, title, phaseSkills, phaseCourses, duration, explanation));
        }

        String summary = String.format("Structured %d-phase learning path for %s based on prerequisite dependencies.",
                phases.size(), context.targetCareer());

        return PersonalizedLearningPathResponse.ok(
                context.userId(),
                context.targetCareer(),
                summary,
                phases,
                "FALLBACK_RULE_BASED",
                "Rule-Based Scoring Engine"
        );
    }
}
