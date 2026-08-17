package com.learningpath.service;

import com.learningpath.dto.*;
import com.learningpath.entity.User;
import com.learningpath.exception.EmailAlreadyExistsException;
import com.learningpath.exception.ResourceNotFoundException;
import com.learningpath.repository.UserRepository;
import com.learningpath.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .targetCareer("Frontend Developer")
                .build();
    }

    @Test
    @DisplayName("1. Successful signup saves hashed password and returns safe response")
    void testSignup_Success() {
        SignupRequest request = new SignupRequest(
                "John Doe",
                "john@example.com",
                "Secret123",
                "Frontend Developer",
                null, null, null, null
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return u;
        });

        SignupResponse response = authService.signup(request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.message()).contains("successfully");
        verify(passwordEncoder).encode("Secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("2. Signup with duplicate email throws EmailAlreadyExistsException")
    void testSignup_DuplicateEmail() {
        SignupRequest request = new SignupRequest(
                "John Doe",
                "john@example.com",
                "Secret123",
                null, null, null, null, null
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("3. Successful login returns valid JWT token and user info")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "Secret123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("Secret123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(), eq("john@example.com"))).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().email()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("4. Login with wrong password throws BadCredentialsException")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest("john@example.com", "WrongPassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("5. Login with unknown email throws BadCredentialsException")
    void testLogin_UnknownEmail() {
        LoginRequest request = new LoginRequest("unknown@example.com", "Secret123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("6. Get current user returns full profile")
    void testGetCurrentUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        AuthenticatedUserResponse response = authService.getCurrentUser(userId);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.targetCareer()).isEqualTo("Frontend Developer");
    }

    @Test
    @DisplayName("7. Get current user with unknown ID throws ResourceNotFoundException")
    void testGetCurrentUser_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
