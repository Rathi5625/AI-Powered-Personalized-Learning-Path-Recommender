package com.learningpath.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.dto.LoginRequest;
import com.learningpath.dto.SignupRequest;
import com.learningpath.dto.SignupResponse;
import com.learningpath.entity.User;
import com.learningpath.entity.enums.UserRole;
import com.learningpath.repository.UserRepository;
import com.learningpath.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin_auth_testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "jwt.expiration-ms=86400000",
        "admin.email=admin@learnai.local",
        "admin.password=ChangeThisAdminPassword123!"
})
@Transactional
class AdminAuthenticationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc getMockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("1. Admin seed account exists with BCrypt password hash and ADMIN role")
    void testAdminAccountCreated_AndBcryptPasswordVerified() {
        User admin = userRepository.findByEmail("admin@learnai.local").orElse(null);
        assertThat(admin).isNotNull();
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getPasswordHash()).isNotNull();
        assertThat(passwordEncoder.matches("ChangeThisAdminPassword123!", admin.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("2. Admin login succeeds and returns JWT with ADMIN role")
    void testAdminLogin_ReturnsAdminJwt() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@learnai.local", "ChangeThisAdminPassword123!");

        getMockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("admin@learnai.local"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("3. JWT contains ADMIN role claim")
    void testJwtContainsAdminRoleClaim() {
        User admin = userRepository.findByEmail("admin@learnai.local").orElseThrow();
        String token = jwtService.generateToken(admin.getId(), admin.getEmail(), "ADMIN");

        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtService.extractEmail(token)).isEqualTo("admin@learnai.local");
        assertThat(jwtService.extractUserId(token)).isEqualTo(admin.getId());
    }

    @Test
    @DisplayName("4. ADMIN accessing GET /api/admin/me returns 200 OK")
    void testAdminAccess_GetAdminMe_Success() throws Exception {
        User admin = userRepository.findByEmail("admin@learnai.local").orElseThrow();
        String token = jwtService.generateToken(admin.getId(), admin.getEmail(), "ADMIN");

        getMockMvc().perform(get("/api/admin/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.message").value("Admin authentication successful"))
                .andExpect(jsonPath("$.email").value("admin@learnai.local"));
    }

    @Test
    @DisplayName("5. USER accessing GET /api/admin/me returns 403 FORBIDDEN")
    void testUserAccess_GetAdminMe_Forbidden() throws Exception {
        SignupResponse normalUser = authService.signup(new SignupRequest(
                "Normal Learner", "learner." + UUID.randomUUID() + "@example.com", "Password123!",
                "Frontend Developer", null, null, null, null
        ));

        String userToken = jwtService.generateToken(normalUser.userId(), normalUser.email(), "USER");

        getMockMvc().perform(get("/api/admin/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. Unauthenticated request to GET /api/admin/me returns 401 UNAUTHORIZED")
    void testUnauthenticatedAccess_GetAdminMe_Unauthorized() throws Exception {
        getMockMvc().perform(get("/api/admin/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. Normal user login returns role USER in token and payload")
    void testNormalUserLogin_ReturnsUserRole() throws Exception {
        String email = "learner." + UUID.randomUUID() + "@example.com";
        authService.signup(new SignupRequest(
                "Learner One", email, "Password123!",
                "Data Engineer", null, null, null, null
        ));

        LoginRequest loginRequest = new LoginRequest(email, "Password123!");

        getMockMvc().perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("USER"));
    }
}
