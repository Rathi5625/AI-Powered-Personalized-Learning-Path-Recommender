package com.learningpath.controller;

import com.learningpath.dto.CourseSkillRequest;
import com.learningpath.dto.CourseSkillResponse;
import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.service.CourseSkillService;
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

class CourseSkillControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseSkillService courseSkillService;

    @InjectMocks
    private CourseSkillController courseSkillController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(courseSkillController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addCourseSkillShouldReturn201Created() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UUID courseSkillId = UUID.randomUUID();

        String json = """
                {
                  "skillId": "%s",
                  "coverageLevel": "INTERMEDIATE",
                  "importance": "CRITICAL",
                  "targetProficiency": "INTERMEDIATE",
                  "isPrimarySkill": true
                }
                """.formatted(skillId);

        CourseSkillResponse response = new CourseSkillResponse(
                courseSkillId,
                courseId,
                "Spring Boot 3 Fundamentals",
                skillId,
                "Spring Boot",
                "Backend Framework",
                CoverageLevel.INTERMEDIATE,
                SkillPriority.CRITICAL,
                ProficiencyLevel.INTERMEDIATE,
                true,
                Instant.now(),
                Instant.now()
        );

        when(courseSkillService.addCourseSkill(eq(courseId), any(CourseSkillRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses/{courseId}/skills", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(courseSkillId.toString()))
                .andExpect(jsonPath("$.skillName").value("Spring Boot"))
                .andExpect(jsonPath("$.coverageLevel").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.importance").value("CRITICAL"));
    }

    @Test
    void addDuplicateCourseSkillShouldReturn409Conflict() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        String json = """
                {
                  "skillId": "%s",
                  "coverageLevel": "INTERMEDIATE",
                  "importance": "CRITICAL"
                }
                """.formatted(skillId);

        when(courseSkillService.addCourseSkill(eq(courseId), any(CourseSkillRequest.class)))
                .thenThrow(new DuplicateResourceException("Course already has skill 'Spring Boot' mapped"));

        mockMvc.perform(post("/api/courses/{courseId}/skills", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Course already has skill 'Spring Boot' mapped"));
    }

    @Test
    void getCourseSkillsShouldReturnListOfSkills() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        UUID courseSkillId = UUID.randomUUID();

        CourseSkillResponse response = new CourseSkillResponse(
                courseSkillId,
                courseId,
                "Spring Boot 3 Fundamentals",
                skillId,
                "Spring Boot",
                "Backend Framework",
                CoverageLevel.INTERMEDIATE,
                SkillPriority.CRITICAL,
                ProficiencyLevel.INTERMEDIATE,
                true,
                Instant.now(),
                Instant.now()
        );

        when(courseSkillService.getCourseSkills(courseId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/courses/{courseId}/skills", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skillName").value("Spring Boot"));
    }

    @Test
    void removeCourseSkillShouldReturn204NoContent() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        doNothing().when(courseSkillService).removeCourseSkill(courseId, skillId);

        mockMvc.perform(delete("/api/courses/{courseId}/skills/{skillId}", courseId, skillId))
                .andExpect(status().isNoContent());
    }
}
