package com.learningpath.controller;

import com.learningpath.dto.CareerRequest;
import com.learningpath.dto.CareerResponse;
import com.learningpath.exception.DuplicateResourceException;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.service.CareerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CareerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CareerService careerService;

    @InjectMocks
    private CareerController careerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(careerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createCareerShouldReturn201Created() throws Exception {
        String json = """
                {
                  "name": "Java Backend Developer",
                  "description": "Backend services & microservices",
                  "category": "Software Engineering"
                }
                """;

        UUID careerId = UUID.randomUUID();
        CareerResponse response = new CareerResponse(
                careerId,
                "Java Backend Developer",
                "Backend services & microservices",
                "Software Engineering",
                Instant.now(),
                Instant.now()
        );

        when(careerService.createCareer(any(CareerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/careers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(careerId.toString()))
                .andExpect(jsonPath("$.name").value("Java Backend Developer"))
                .andExpect(jsonPath("$.category").value("Software Engineering"));
    }

    @Test
    void createDuplicateCareerShouldReturn409Conflict() throws Exception {
        String json = """
                {
                  "name": "Java Backend Developer",
                  "description": "Backend services & microservices",
                  "category": "Software Engineering"
                }
                """;

        when(careerService.createCareer(any(CareerRequest.class)))
                .thenThrow(new DuplicateResourceException("Career with name 'Java Backend Developer' already exists"));

        mockMvc.perform(post("/api/careers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Career with name 'Java Backend Developer' already exists"));
    }

    @Test
    void searchCareersByNameShouldReturnMatchingCareers() throws Exception {
        UUID careerId = UUID.randomUUID();
        CareerResponse response = new CareerResponse(
                careerId,
                "Java Backend Developer",
                "Backend services & microservices",
                "Software Engineering",
                Instant.now(),
                Instant.now()
        );

        when(careerService.searchCareersByName("backend")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/careers/search").param("name", "backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java Backend Developer"));
    }
}
