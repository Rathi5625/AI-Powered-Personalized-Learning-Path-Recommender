package com.learningpath.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.dto.*;
import com.learningpath.exception.GlobalExceptionHandler;
import com.learningpath.security.UserPrincipal;
import com.learningpath.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UUID userId;
    private UserPrincipal mockPrincipal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockPrincipal = new UserPrincipal(
                userId,
                "john@example.com",
                "John Doe",
                "hashed",
                Collections.emptyList()
        );

        // Custom resolver for @AuthenticationPrincipal in standalone MockMvc
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                String authHeader = webRequest.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer valid")) {
                    return mockPrincipal;
                }
                return null;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/signup returns 201 CREATED with safe response")
    void testSignup_Success() throws Exception {
        SignupRequest request = new SignupRequest(
                "John Doe", "john@example.com", "Secret123", "Frontend", null, null, null, null
        );
        SignupResponse response = new SignupResponse(userId, "John Doe", "john@example.com", "Account created successfully");

        when(authService.signup(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.message").value("Account created successfully"));
    }

    @Test
    @DisplayName("POST /api/auth/signup with invalid email returns 400 BAD REQUEST")
    void testSignup_InvalidEmail() throws Exception {
        SignupRequest request = new SignupRequest(
                "John Doe", "invalid-email", "Secret123", null, null, null, null, null
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 OK with JWT token")
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "Secret123");
        AuthResponse response = new AuthResponse(
                "mock.jwt.token", "Bearer", 3600L, new UserSummaryResponse(userId, "John Doe", "john@example.com")
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login with wrong password returns 401 UNAUTHORIZED")
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("GET /api/auth/me returns 200 OK with authenticated user profile")
    void testGetCurrentUser_Success() throws Exception {
        AuthenticatedUserResponse userResponse = new AuthenticatedUserResponse(
                userId, "John Doe", "john@example.com", "Frontend Developer", null, 2, null, null, Instant.now(), Instant.now()
        );

        when(authService.getCurrentUser(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid_token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.targetCareer").value("Frontend Developer"));
    }

    @Test
    @DisplayName("GET /api/auth/me without authentication returns 401 UNAUTHORIZED")
    void testGetCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
