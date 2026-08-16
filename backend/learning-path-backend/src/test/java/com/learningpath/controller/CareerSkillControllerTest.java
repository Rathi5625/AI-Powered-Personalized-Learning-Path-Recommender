package com.learningpath.controller;

import com.learningpath.dto.CareerSkillRequest;
import com.learningpath.dto.CareerSkillResponse;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.service.CareerSkillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CareerSkillControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CareerSkillService careerSkillService;

    @InjectMocks
    private CareerSkillController careerSkillController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(careerSkillController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addCareerSkillShouldReturn201Created() throws Exception {
        UUID careerId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UUID careerSkillId = UUID.randomUUID();

        String json = """
                {
                  "skillId": "%s",
                  "priority": "CRITICAL",
                  "requiredProficiency": "INTERMEDIATE",
                  "isMandatory": true
                }
                """.formatted(skillId);

        CareerSkillResponse response = new CareerSkillResponse(
                careerSkillId,
                careerId,
                "Java Backend Developer",
                skillId,
                "Java",
                "Programming",
                SkillPriority.CRITICAL,
                ProficiencyLevel.INTERMEDIATE,
                true,
                Instant.now(),
                Instant.now()
        );

        when(careerSkillService.addCareerSkill(eq(careerId), any(CareerSkillRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/careers/{careerId}/skills", careerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(careerSkillId.toString()))
                .andExpect(jsonPath("$.skillName").value("Java"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.requiredProficiency").value("INTERMEDIATE"));
    }

    @Test
    void addDuplicateCareerSkillShouldReturn409Conflict() throws Exception {
        UUID careerId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        String json = """
                {
                  "skillId": "%s",
                  "priority": "CRITICAL",
                  "requiredProficiency": "INTERMEDIATE",
                  "isMandatory": true
                }
                """.formatted(skillId);

        when(careerSkillService.addCareerSkill(eq(careerId), any(CareerSkillRequest.class)))
                .thenThrow(new DuplicateResourceException("Career already has required skill 'Java' assigned"));

        mockMvc.perform(post("/api/careers/{careerId}/skills", careerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Career already has required skill 'Java' assigned"));
    }

    @Test
    void getCareerSkillsShouldReturnListOfSkills() throws Exception {
        UUID careerId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UUID careerSkillId = UUID.randomUUID();

        CareerSkillResponse response = new CareerSkillResponse(
                careerSkillId,
                careerId,
                "Java Backend Developer",
                skillId,
                "Java",
                "Programming",
                SkillPriority.CRITICAL,
                ProficiencyLevel.INTERMEDIATE,
                true,
                Instant.now(),
                Instant.now()
        );

        when(careerSkillService.getCareerSkills(careerId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/careers/{careerId}/skills", careerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillName").value("Java"));
    }

    @Test
    void removeCareerSkillShouldReturn204NoContent() throws Exception {
        UUID careerId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        doNothing().when(careerSkillService).removeCareerSkill(careerId, skillId);

        mockMvc.perform(delete("/api/careers/{careerId}/skills/{skillId}", careerId, skillId))
                .andExpect(status().isNoContent());
    }
}
