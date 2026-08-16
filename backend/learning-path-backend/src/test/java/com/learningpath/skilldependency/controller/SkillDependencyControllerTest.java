package com.learningpath.skilldependency.controller;

import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.skilldependency.dto.*;
import com.learningpath.skilldependency.service.SkillDependencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SkillDependencyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SkillDependencyService dependencyService;

    @InjectMocks
    private SkillDependencyController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetPrerequisites_Success() throws Exception {
        when(dependencyService.getPrerequisites("JavaScript"))
                .thenReturn(new PrerequisitesResponse("JavaScript", true, List.of("HTML", "CSS"), List.of("Internet Basics", "HTML", "CSS")));

        mockMvc.perform(get("/api/skills/dependencies/JavaScript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skill").value("JavaScript"))
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.directPrerequisites[0]").value("HTML"))
                .andExpect(jsonPath("$.directPrerequisites[1]").value("CSS"));
    }

    @Test
    void testGetDependents_Success() throws Exception {
        when(dependencyService.getDependents("HTML"))
                .thenReturn(new DependentsResponse("HTML", true, List.of("CSS", "JavaScript"), List.of("CSS", "JavaScript", "REST APIs")));

        mockMvc.perform(get("/api/skills/dependencies/HTML/dependents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skill").value("HTML"))
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.directDependents[0]").value("CSS"));
    }

    @Test
    void testGetLearningOrder_Success() throws Exception {
        String json = """
                {
                  "skills": ["CSS", "HTML", "JavaScript"]
                }
                """;

        when(dependencyService.getLearningOrder(any()))
                .thenReturn(LearningOrderResponse.ok(List.of("Internet Basics", "HTML", "CSS", "JavaScript"), List.of()));

        mockMvc.perform(post("/api/skills/dependencies/learning-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.learningOrder[0]").value("Internet Basics"))
                .andExpect(jsonPath("$.learningOrder[3]").value("JavaScript"));
    }

    @Test
    void testGetMissingPrerequisites_Success() throws Exception {
        String json = """
                {
                  "currentSkills": ["Internet Basics", "HTML"],
                  "targetSkills": ["JavaScript"]
                }
                """;

        when(dependencyService.getMissingPrerequisites(any(), any()))
                .thenReturn(MissingPrerequisitesResponse.ok(List.of("CSS", "JavaScript"), List.of("Internet Basics", "HTML"), List.of()));

        mockMvc.perform(post("/api/skills/dependencies/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.missingPrerequisites[0]").value("CSS"))
                .andExpect(jsonPath("$.satisfiedPrerequisites[0]").value("Internet Basics"));
    }
}
