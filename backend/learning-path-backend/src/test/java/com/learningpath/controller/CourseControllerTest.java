package com.learningpath.controller;

import com.learningpath.dto.CourseRequest;
import com.learningpath.dto.CourseResponse;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.service.CourseService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(courseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createCourseShouldReturn201Created() throws Exception {
        String json = """
                {
                  "title": "Spring Boot 3 Fundamentals",
                  "description": "Core Spring Boot principles",
                  "provider": "Coursera",
                  "difficulty": "BEGINNER",
                  "durationHours": 20.0,
                  "courseType": "VIDEO_COURSE",
                  "language": "English",
                  "rating": 4.80,
                  "price": 0.00,
                  "isFree": true
                }
                """;

        UUID courseId = UUID.randomUUID();
        CourseResponse response = new CourseResponse(
                courseId,
                "Spring Boot 3 Fundamentals",
                "Core Spring Boot principles",
                "Coursera",
                null,
                CourseDifficulty.BEGINNER,
                20.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.80"),
                BigDecimal.ZERO,
                true,
                Instant.now(),
                Instant.now()
        );

        when(courseService.createCourse(any(CourseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(courseId.toString()))
                .andExpect(jsonPath("$.title").value("Spring Boot 3 Fundamentals"))
                .andExpect(jsonPath("$.isFree").value(true));
    }

    @Test
    void searchCoursesByTitleShouldReturnMatchingCourses() throws Exception {
        UUID courseId = UUID.randomUUID();
        CourseResponse response = new CourseResponse(
                courseId,
                "Spring Boot 3 Fundamentals",
                "Core Spring Boot principles",
                "Coursera",
                null,
                CourseDifficulty.BEGINNER,
                20.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.80"),
                BigDecimal.ZERO,
                true,
                Instant.now(),
                Instant.now()
        );

        when(courseService.searchCoursesByTitle("spring")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/courses/search").param("title", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring Boot 3 Fundamentals"));
    }

    @Test
    void filterCoursesShouldReturnFilteredPage() throws Exception {
        UUID courseId = UUID.randomUUID();
        CourseResponse response = new CourseResponse(
                courseId,
                "Spring Boot 3 Fundamentals",
                "Core Spring Boot principles",
                "Coursera",
                null,
                CourseDifficulty.BEGINNER,
                20.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.80"),
                BigDecimal.ZERO,
                true,
                Instant.now(),
                Instant.now()
        );

        when(courseService.filterCourses(eq(CourseDifficulty.BEGINNER), any(), any(), eq(true), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/courses/filter")
                        .param("difficulty", "BEGINNER")
                        .param("isFree", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].difficulty").value("BEGINNER"))
                .andExpect(jsonPath("$.content[0].isFree").value(true));
    }
}
