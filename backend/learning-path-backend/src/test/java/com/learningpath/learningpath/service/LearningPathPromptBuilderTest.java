package com.learningpath.learningpath.service;

import com.learningpath.learningpath.dto.LearningPathContext;
import com.learningpath.learningpath.dto.RecommendedCourseItem;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LearningPathPromptBuilderTest {

    private LearningPathPromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new LearningPathPromptBuilder(new ObjectMapper());
    }

    @Test
    void testBuildPrompt_FormatsContextAndGroundingInstructionsCorrectly() {
        UUID courseId = UUID.randomUUID();
        RecommendedCourseItem course = new RecommendedCourseItem(
                courseId,
                "Spring Boot Masterclass",
                "Coursera",
                0.92,
                "INTERMEDIATE",
                List.of("Java", "Spring Boot")
        );

        LearningPathContext context = new LearningPathContext(
                UUID.randomUUID(),
                "Alice Smith",
                "Java Developer",
                "INTERMEDIATE",
                "MIXED",
                2.0,
                List.of("Java"),
                List.of("Spring Boot"),
                List.of("Java", "Spring Boot"),
                List.of(course)
        );

        String prompt = promptBuilder.buildPrompt(context);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Alice Smith"));
        assertTrue(prompt.contains("Java Developer"));
        assertTrue(prompt.contains(courseId.toString()));
        assertTrue(prompt.contains("Spring Boot Masterclass"));
        assertTrue(prompt.contains("CRITICAL GROUNDING RULES"));
    }
}
