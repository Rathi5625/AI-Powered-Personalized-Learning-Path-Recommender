package com.learningpath.ai.reasoning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.reasoning.dto.*;
import com.learningpath.ai.reasoning.prompt.GeminiReasoningPromptBuilder;
import com.learningpath.ai.reasoning.validation.GeminiReasoningValidator;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiReasoningService {

    private final GeminiClient geminiClient;
    private final GeminiReasoningPromptBuilder promptBuilder;
    private final GeminiReasoningValidator validator;
    private final ObjectMapper objectMapper;

    public GeminiReasoningResult generateReasoning(GeminiReasoningInput input) {
        if (input == null || input.candidateCourses() == null || input.candidateCourses().isEmpty()) {
            return new GeminiReasoningResult(
                    "No candidate courses available for evaluation.",
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    false
            );
        }

        try {
            String promptText = promptBuilder.buildReasoningPrompt(input);
            long startTime = System.currentTimeMillis();

            AiTestResponse aiResponse = geminiClient.generateContent(promptText);
            long duration = System.currentTimeMillis() - startTime;

            if (aiResponse.success() && aiResponse.response() != null && !aiResponse.response().isBlank()) {
                String cleanJson = extractJson(aiResponse.response());
                GeminiReasoningResult rawResult = objectMapper.readValue(cleanJson, GeminiReasoningResult.class);

                GeminiReasoningResult validatedResult = validator.validateAndSanitize(rawResult, input);
                if (validatedResult != null) {
                    log.info("[GeminiReasoningService] Successfully generated AI reasoning in {} ms (Model: {})",
                            duration, aiResponse.model());
                    return validatedResult;
                }
            } else {
                log.warn("[GeminiReasoningService] Gemini invocation unsuccessful or returned empty: {}. Using deterministic fallback.",
                        aiResponse.error());
            }
        } catch (Exception e) {
            log.warn("[GeminiReasoningService] Exception during AI reasoning generation: {}. Using deterministic fallback.",
                    e.getMessage());
        }

        // Deterministic Fallback
        return buildDeterministicFallback(input);
    }

    public GeminiReasoningResult buildDeterministicFallback(GeminiReasoningInput input) {
        LearnerProfileDto learner = input.learner();
        List<CandidateCourseDto> candidates = input.candidateCourses();
        Map<String, SkillGapItemResponse> gapMap = new HashMap<>();
        if (input.skillGaps() != null) {
            for (SkillGapItemResponse g : input.skillGaps()) {
                gapMap.put(g.skillName().toLowerCase(), g);
            }
        }

        String summary = String.format(
                "Personalized course recommendations sequenced for your target role as %s. Courses are prioritized by skill gap severity and foundational prerequisites.",
                learner != null && learner.careerGoal() != null ? learner.careerGoal() : "Software Professional"
        );

        List<GeminiCourseExplanationDto> recs = new ArrayList<>();
        List<GeminiCourseSequenceItemDto> sequence = new ArrayList<>();

        for (int i = 0; i < candidates.size(); i++) {
            CandidateCourseDto c = candidates.get(i);
            String primarySkill = (c.gapSkillsAddressed() != null && !c.gapSkillsAddressed().isEmpty())
                    ? c.gapSkillsAddressed().get(0)
                    : (c.skillsCovered() != null && !c.skillsCovered().isEmpty() ? c.skillsCovered().get(0) : "Core Skill");

            SkillGapItemResponse gap = gapMap.get(primarySkill.toLowerCase());
            String reason;
            if (gap != null) {
                reason = String.format("Directly addresses your %s gap in %s (Target: %s, Priority: %s).",
                        gap.gapType(), gap.skillName(), gap.requiredProficiency(), gap.priority());
            } else {
                reason = String.format("Builds foundational competence in %s for your %s career trajectory.",
                        primarySkill, learner != null && learner.careerGoal() != null ? learner.careerGoal() : "target role");
            }

            String prereqReason = String.format("Positioned in prerequisite sequence according to canonical curriculum dependencies for %s.", primarySkill);
            String effort = String.format("%s difficulty (%s)", c.difficulty(), c.courseType());

            recs.add(new GeminiCourseExplanationDto(
                    c.courseId(),
                    reason,
                    c.skillsCovered(),
                    c.gapSkillsAddressed(),
                    prereqReason,
                    effort,
                    i + 1
            ));

            sequence.add(new GeminiCourseSequenceItemDto(
                    c.courseId(),
                    i + 1,
                    String.format("Step %d: Complete %s to establish required proficiency in %s.", i + 1, c.title(), primarySkill)
            ));
        }

        List<String> adaptationNotes = List.of(
                "Your learning path will automatically adapt as you complete courses and verify skill proficiencies.",
                "High-priority skill gaps are sequenced first to unlock downstream dependent milestones."
        );

        return new GeminiReasoningResult(summary, recs, sequence, adaptationNotes, false);
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
