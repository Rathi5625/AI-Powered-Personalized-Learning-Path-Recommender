package com.learningpath.controller;

import com.learningpath.dto.UserCreateRequest;
import com.learningpath.dto.UserResponse;
import com.learningpath.dto.UserUpdateRequest;
import com.learningpath.entity.enums.ExperienceLevel;
import com.learningpath.entity.enums.LearningStyle;
import com.learningpath.entity.enums.PreferredContentType;
import com.learningpath.exception.EmailAlreadyExistsException;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.service.UserService;
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

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createUserShouldReturn201Created() throws Exception {
        String requestJson = """
                {
                  "name": "Alex",
                  "email": "alex@example.com",
                  "careerGoal": "JAVA_BACKEND_DEVELOPER",
                  "experienceLevel": "BEGINNER",
                  "dailyLearningHours": 2,
                  "learningStyle": "PRACTICAL",
                  "preferredContentType": "VIDEO"
                }
                """;

        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(
                userId,
                "Alex",
                "alex@example.com",
                "JAVA_BACKEND_DEVELOPER",
                ExperienceLevel.BEGINNER,
                2,
                LearningStyle.PRACTICAL,
                PreferredContentType.VIDEO,
                Instant.now(),
                Instant.now()
        );

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alex"))
                .andExpect(jsonPath("$.email").value("alex@example.com"))
                .andExpect(jsonPath("$.careerGoal").value("JAVA_BACKEND_DEVELOPER"))
                .andExpect(jsonPath("$.experienceLevel").value("BEGINNER"))
                .andExpect(jsonPath("$.dailyLearningHours").value(2))
                .andExpect(jsonPath("$.learningStyle").value("PRACTICAL"))
                .andExpect(jsonPath("$.preferredContentType").value("VIDEO"));
    }

    @Test
    void createUserWithBlankNameShouldReturn400BadRequest() throws Exception {
        String requestJson = """
                {
                  "name": "",
                  "email": "alex@example.com",
                  "careerGoal": "JAVA_BACKEND_DEVELOPER",
                  "experienceLevel": "BEGINNER",
                  "dailyLearningHours": 2,
                  "learningStyle": "PRACTICAL",
                  "preferredContentType": "VIDEO"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createUserWithDuplicateEmailShouldReturn409Conflict() throws Exception {
        String requestJson = """
                {
                  "name": "Alex",
                  "email": "alex@example.com",
                  "careerGoal": "JAVA_BACKEND_DEVELOPER",
                  "experienceLevel": "BEGINNER",
                  "dailyLearningHours": 2,
                  "learningStyle": "PRACTICAL",
                  "preferredContentType": "VIDEO"
                }
                """;

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("User with email 'alex@example.com' already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User with email 'alex@example.com' already exists"));
    }

    @Test
    void getUserByIdShouldReturn200Ok() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(
                userId,
                "Alex",
                "alex@example.com",
                "JAVA_BACKEND_DEVELOPER",
                ExperienceLevel.BEGINNER,
                2,
                LearningStyle.PRACTICAL,
                PreferredContentType.VIDEO,
                Instant.now(),
                Instant.now()
        );

        when(userService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Alex"));
    }

    @Test
    void getUserByIdNotFoundShouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + userId));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateUserShouldReturn200Ok() throws Exception {
        UUID userId = UUID.randomUUID();
        String requestJson = """
                {
                  "name": "Alex Updated",
                  "careerGoal": "FULLSTACK_DEVELOPER",
                  "experienceLevel": "INTERMEDIATE",
                  "dailyLearningHours": 4,
                  "learningStyle": "PRACTICAL",
                  "preferredContentType": "PROJECT"
                }
                """;

        UserResponse response = new UserResponse(
                userId,
                "Alex Updated",
                "alex@example.com",
                "FULLSTACK_DEVELOPER",
                ExperienceLevel.INTERMEDIATE,
                4,
                LearningStyle.PRACTICAL,
                PreferredContentType.PROJECT,
                Instant.now(),
                Instant.now()
        );

        when(userService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex Updated"))
                .andExpect(jsonPath("$.dailyLearningHours").value(4));
    }

    @Test
    void deleteUserShouldReturn204NoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllUsersShouldReturnPaginatedUsers() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = new UserResponse(
                userId,
                "Alex",
                "alex@example.com",
                "JAVA_BACKEND_DEVELOPER",
                ExperienceLevel.BEGINNER,
                2,
                LearningStyle.PRACTICAL,
                PreferredContentType.VIDEO,
                Instant.now(),
                Instant.now()
        );

        when(userService.getAllUsers(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alex"));
    }
}

