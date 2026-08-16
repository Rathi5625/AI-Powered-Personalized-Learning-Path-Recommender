package com.learningpath.learningpath.controller;

import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.learningpath.dto.LearningPathPhase;
import com.learningpath.learningpath.dto.PersonalizedLearningPathResponse;
import com.learningpath.learningpath.service.PersonalizedLearningPathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PersonalizedLearningPathControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PersonalizedLearningPathService learningPathService;

    @InjectMocks
    private PersonalizedLearningPathController controller;

    private UUID userId;
    private UUID careerId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();
        careerId = UUID.randomUUID();
    }

    @Test
    void testGenerateLearningPath_Success() throws Exception {
        String json = String.format("""
                {
                  "userId": "%s",
                  "careerId": "%s"
                }
                """, userId, careerId);

        LearningPathPhase phase = new LearningPathPhase(
                1,
                "Phase 1: Web Basics",
                List.of("HTML", "CSS"),
                List.of(),
                "2 weeks",
                "Start with HTML & CSS"
        );

        PersonalizedLearningPathResponse response = PersonalizedLearningPathResponse.ok(
                userId,
                "Frontend Developer",
                "Custom learning journey",
                List.of(phase),
                "GEMINI",
                "gemini-1.5-flash"
        );

        when(learningPathService.generateLearningPath(eq(userId), eq(careerId)))
                .thenReturn(response);

        mockMvc.perform(post("/api/learning-paths/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.targetCareer").value("Frontend Developer"))
                .andExpect(jsonPath("$.provider").value("GEMINI"))
                .andExpect(jsonPath("$.phases[0].phaseTitle").value("Phase 1: Web Basics"));
    }

    @Test
    void testGenerateLearningPath_MissingUserId_Returns400BadRequest() throws Exception {
        String invalidJson = """
                {
                  "careerId": "b1a78945-8f64-4e2b-b6d8-111111111111"
                }
                """;

        mockMvc.perform(post("/api/learning-paths/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
