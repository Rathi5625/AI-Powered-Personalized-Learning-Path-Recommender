package com.learningpath.ai.reasoning.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.ai.reasoning.dto.GeminiReasoningInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiReasoningPromptBuilder {

    private final ObjectMapper objectMapper;

    public String buildReasoningPrompt(GeminiReasoningInput input) {
        try {
            String inputJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);

            return """
                    You are an AI learning-path reasoning and course recommendation engine for LearnAI.
                    
                    CRITICAL ANTI-HALLUCINATION & INTEGRITY RULES:
                    1. You may ONLY reason over the supplied learner profile, canonical skill gaps, prerequisite relationships, and supplied course candidates.
                    2. You MUST NOT invent courses, course IDs, skills, URLs, providers, ratings, prices, or prerequisites.
                    3. You MUST select and explain ONLY courses that exist in the supplied "candidateCourses" list.
                    4. Every "courseId" in your output MUST match an exact UUID from the candidate courses.
                    5. The sequence of courses in "learningSequence" MUST strictly respect the supplied "prerequisiteOrder" (e.g. foundational prerequisite skills before advanced dependent skills).
                    6. Return ONLY a valid, parseable JSON object without markdown formatting, code fences, or additional text.
                    
                    REQUIRED JSON OUTPUT SCHEMA:
                    {
                      "summary": "Overall synthesis explaining why this sequence of courses is tailored to the learner's career goal and gaps.",
                      "recommendations": [
                        {
                          "courseId": "<exact UUID from candidateCourses>",
                          "reason": "Personalized explanation connecting this specific course to the learner's career goal, skill gap, and experience level.",
                          "skillsAddressed": ["<skill names>"],
                          "gapSkillsAddressed": ["<skill names with active gaps>"],
                          "prerequisiteReason": "Explanation of where this course sits in the prerequisite graph relative to other skills.",
                          "estimatedEffort": "e.g., 6 hours or 2 weeks at learner's daily pace",
                          "priority": 1
                        }
                      ],
                      "learningSequence": [
                        {
                          "courseId": "<exact UUID from candidateCourses>",
                          "order": 1,
                          "reason": "Why this course should be completed at this stage."
                        }
                      ],
                      "adaptationNotes": [
                        "Notes on how the path can adapt as the learner progresses or masters skills."
                      ]
                    }
                    
                    INPUT DATA:
                    %s
                    """.formatted(inputJson);
        } catch (Exception e) {
            log.error("[GeminiReasoningPromptBuilder] Failed to serialize reasoning input: {}", e.getMessage());
            throw new IllegalStateException("Failed to construct Gemini reasoning prompt", e);
        }
    }
}
