package com.learningpath.recommendation.controller;

import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.recommendation.domain.GapSeverity;
import com.learningpath.recommendation.domain.GapType;
import com.learningpath.recommendation.dto.SkillGapAnalysisResponse;
import com.learningpath.recommendation.dto.SkillGapItemResponse;
import com.learningpath.recommendation.service.SkillGapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SkillGapControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SkillGapService skillGapService;

    @InjectMocks
    private SkillGapController skillGapController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(skillGapController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSkillGapsShouldReturnAnalysisResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID careerId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        SkillGapItemResponse item = new SkillGapItemResponse(
                skillId,
                "Spring Boot",
                "Backend Framework",
                "NONE",
                ProficiencyLevel.BEGINNER,
                GapType.FULL_GAP,
                GapSeverity.CRITICAL,
                SkillPriority.CRITICAL,
                true,
                "Spring Boot is a critical required skill for this career and is currently missing from the learner profile."
        );

        SkillGapAnalysisResponse response = new SkillGapAnalysisResponse(
                userId,
                "John Doe",
                careerId,
                "Java Backend Developer",
                10,
                1,
                2,
                7,
                78.5,
                List.of(item)
        );

        when(skillGapService.analyzeSkillGap(userId, careerId)).thenReturn(response);

        mockMvc.perform(get("/api/users/{userId}/skill-gaps", userId)
                        .param("careerId", careerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.careerName").value("Java Backend Developer"))
                .andExpect(jsonPath("$.totalRequiredSkills").value(10))
                .andExpect(jsonPath("$.fullGaps").value(7))
                .andExpect(jsonPath("$.overallGapScore").value(78.5))
                .andExpect(jsonPath("$.gaps[0].skillName").value("Spring Boot"))
                .andExpect(jsonPath("$.gaps[0].gapType").value("FULL_GAP"));
    }
}
