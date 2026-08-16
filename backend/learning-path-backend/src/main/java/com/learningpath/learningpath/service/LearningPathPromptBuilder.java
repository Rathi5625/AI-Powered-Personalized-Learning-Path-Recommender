package com.learningpath.learningpath.service;

import com.learningpath.learningpath.dto.LearningPathContext;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathPromptBuilder {

    private final ObjectMapper objectMapper;

    public String buildPrompt(LearningPathContext context) {
        try {
            List<Map<String, Object>> courseSummaries = context.candidateCourses().stream()
                    .map(c -> Map.<String, Object>of(
                            "courseId", c.courseId().toString(),
                            "title", c.courseTitle(),
                            "provider", c.provider(),
                            "difficulty", c.difficulty(),
                            "score", c.score(),
                            "skillsCovered", c.skillsCovered()
                    ))
                    .toList();

            String coursesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(courseSummaries);

            return String.format("""
                    You are an expert personalized AI learning path generator.
                    
                    LEARNER PROFILE:
                    - Name: %s
                    - Target Career: %s
                    - Experience Level: %s
                    - Learning Style: %s
                    - Daily Available Hours: %.1f
                    
                    SKILL PREREQUISITE TOPOLOGICAL ORDER:
                    - Current Known Skills: %s
                    - Target Skill Gaps (Required Order): %s
                    
                    AVAILABLE CANDIDATE RECOMMENDED COURSES (STRICT GROUNDING LIST):
                    %s
                    
                    CRITICAL GROUNDING RULES:
                    1. You MUST ONLY use courseIds and titles from the AVAILABLE CANDIDATE RECOMMENDED COURSES list above.
                    2. DO NOT invent, hallucinate, or suggest any fake courses, URLs, or providers.
                    3. Group the learning path into sequential phases based on the Target Skill Gaps Required Order.
                    4. Return ONLY a valid JSON object strictly matching this schema:
                    {
                      "summary": "Brief explanation of the overall learning journey",
                      "phases": [
                        {
                          "phaseNumber": 1,
                          "phaseTitle": "Phase 1: Fundamental Prerequisites",
                          "targetSkills": ["HTML", "CSS"],
                          "courseIds": ["<VALID_COURSE_UUID>"],
                          "estimatedDuration": "2 weeks",
                          "explanation": "Why these courses are taken first"
                        }
                      ]
                    }
                    """,
                    context.fullName(),
                    context.targetCareer(),
                    context.experienceLevel(),
                    context.learningStyle(),
                    context.dailyLearningHours(),
                    context.currentSkills(),
                    context.orderedTargetSkills(),
                    coursesJson
            );
        } catch (Exception e) {
            log.error("[LearningPathPromptBuilder] Error serializing prompt courses: {}", e.getMessage());
            throw new RuntimeException("Failed to build prompt for learning path generation", e);
        }
    }
}
