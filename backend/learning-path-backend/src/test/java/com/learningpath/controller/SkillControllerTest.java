package com.learningpath.controller;

import com.learningpath.dto.SkillRequest;
import com.learningpath.dto.SkillResponse;
import com.learningpath.entity.enums.SkillDifficulty;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.service.SkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SkillControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SkillService skillService;

    @InjectMocks
    private SkillController skillController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(skillController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createSkillShouldReturn201Created() throws Exception {
        String json = """
                {
                  "name": "Java",
                  "category": "Programming",
                  "description": "Core Java Programming",
                  "difficulty": "INTERMEDIATE"
                }
                """;

        UUID skillId = UUID.randomUUID();
        SkillResponse response = new SkillResponse(
                skillId,
                "Java",
                "Programming",
                "Core Java Programming",
                SkillDifficulty.INTERMEDIATE,
                Instant.now(),
                Instant.now()
        );

        when(skillService.createSkill(any(SkillRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(skillId.toString()))
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.category").value("Programming"));
    }

    @Test
    void createDuplicateSkillShouldReturn409Conflict() throws Exception {
        String json = """
                {
                  "name": "Java",
                  "category": "Programming",
                  "description": "Core Java Programming",
                  "difficulty": "INTERMEDIATE"
                }
                """;

        when(skillService.createSkill(any(SkillRequest.class)))
                .thenThrow(new DuplicateResourceException("Skill with name 'Java' already exists"));

        mockMvc.perform(post("/api/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Skill with name 'Java' already exists"));
    }

    @Test
    void getSkillByIdShouldReturn200Ok() throws Exception {
        UUID skillId = UUID.randomUUID();
        SkillResponse response = new SkillResponse(
                skillId,
                "Java",
                "Programming",
                "Core Java Programming",
                SkillDifficulty.INTERMEDIATE,
                Instant.now(),
                Instant.now()
        );

        when(skillService.getSkillById(skillId)).thenReturn(response);

        mockMvc.perform(get("/api/skills/{id}", skillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(skillId.toString()))
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void searchSkillsByNameShouldReturnMatchingSkills() throws Exception {
        UUID skillId = UUID.randomUUID();
        SkillResponse response = new SkillResponse(
                skillId,
                "Java",
                "Programming",
                "Core Java Programming",
                SkillDifficulty.INTERMEDIATE,
                Instant.now(),
                Instant.now()
        );

        when(skillService.searchSkillsByName("java")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/skills/search").param("name", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"));
    }
}
