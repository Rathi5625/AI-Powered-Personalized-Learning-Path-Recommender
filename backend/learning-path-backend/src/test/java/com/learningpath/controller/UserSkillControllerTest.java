package com.learningpath.controller;

import com.learningpath.dto.UserSkillRequest;
import com.learningpath.dto.UserSkillResponse;
import com.learningpath.dto.UserSkillUpdateRequest;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillSource;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.service.UserSkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
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

class UserSkillControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserSkillService userSkillService;

    @InjectMocks
    private UserSkillController userSkillController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userSkillController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addUserSkillShouldReturn201Created() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UUID userSkillId = UUID.randomUUID();

        String json = """
                {
                  "skillId": "%s",
                  "proficiencyLevel": "INTERMEDIATE",
                  "confidence": 85.5,
                  "source": "SELF_REPORTED"
                }
                """.formatted(skillId);

        UserSkillResponse response = new UserSkillResponse(
                userSkillId,
                userId,
                skillId,
                "Java",
                "Programming",
                ProficiencyLevel.INTERMEDIATE,
                new BigDecimal("85.50"),
                SkillSource.SELF_REPORTED,
                false,
                Instant.now(),
                Instant.now(),
                Instant.now()
        );

        when(userSkillService.addUserSkill(eq(userId), any(UserSkillRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/{userId}/skills", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userSkillId.toString()))
                .andExpect(jsonPath("$.skillName").value("Java"))
                .andExpect(jsonPath("$.proficiencyLevel").value("INTERMEDIATE"));
    }

    @Test
    void addDuplicateUserSkillShouldReturn409Conflict() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        String json = """
                {
                  "skillId": "%s",
                  "proficiencyLevel": "INTERMEDIATE",
                  "confidence": 85.5,
                  "source": "SELF_REPORTED"
                }
                """.formatted(skillId);

        when(userSkillService.addUserSkill(eq(userId), any(UserSkillRequest.class)))
                .thenThrow(new DuplicateResourceException("User already has skill 'Java' assigned"));

        mockMvc.perform(post("/api/users/{userId}/skills", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User already has skill 'Java' assigned"));
    }

    @Test
    void getUserSkillsShouldReturnListOfUserSkills() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UUID userSkillId = UUID.randomUUID();

        UserSkillResponse response = new UserSkillResponse(
                userSkillId,
                userId,
                skillId,
                "Java",
                "Programming",
                ProficiencyLevel.INTERMEDIATE,
                new BigDecimal("85.50"),
                SkillSource.SELF_REPORTED,
                false,
                Instant.now(),
                Instant.now(),
                Instant.now()
        );

        when(userSkillService.getUserSkills(userId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/users/{userId}/skills", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillName").value("Java"));
    }

    @Test
    void removeUserSkillShouldReturn204NoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        doNothing().when(userSkillService).removeUserSkill(userId, skillId);

        mockMvc.perform(delete("/api/users/{userId}/skills/{skillId}", userId, skillId))
                .andExpect(status().isNoContent());
    }
}
