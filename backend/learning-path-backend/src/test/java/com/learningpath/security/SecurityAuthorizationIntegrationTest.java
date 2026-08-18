package com.learningpath.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.dto.SignupRequest;
import com.learningpath.dto.SignupResponse;
import com.learningpath.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "jwt.expiration-ms=86400000"
})
@Transactional
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

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
    @DisplayName("1. Public endpoint GET /api/health is accessible without token")
    void testPublicEndpoint_Accessible() throws Exception {
        getMockMvc().perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. Public endpoint GET /api/careers is accessible without token")
    void testCareers_Accessible() throws Exception {
        getMockMvc().perform(get("/api/careers"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("3. Protected user endpoint rejects unauthenticated access with 401 Unauthorized")
    void testProtectedEndpoint_Unauthenticated_Rejected() throws Exception {
        UUID randomUserId = UUID.randomUUID();

        getMockMvc().perform(get("/api/users/" + randomUserId + "/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. User A can access own dashboard with valid JWT token")
    void testUserAccessOwnData_Success() throws Exception {
        SignupResponse userA = authService.signup(new SignupRequest(
                "User A", "user.a." + UUID.randomUUID() + "@example.com", "Password123",
                "Frontend Developer", null, null, null, null
        ));

        String tokenA = jwtService.generateToken(userA.userId(), userA.email());

        getMockMvc().perform(get("/api/users/" + userA.userId() + "/dashboard")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.name").value("User A"));
    }

    @Test
    @DisplayName("5. User A CANNOT access User B's dashboard (Cross-User Access returns 403 FORBIDDEN)")
    void testCrossUserAccess_Forbidden() throws Exception {
        SignupResponse userA = authService.signup(new SignupRequest(
                "User A", "user.a." + UUID.randomUUID() + "@example.com", "Password123",
                "Frontend Developer", null, null, null, null
        ));

        SignupResponse userB = authService.signup(new SignupRequest(
                "User B", "user.b." + UUID.randomUUID() + "@example.com", "Password123",
                "Java Backend Developer", null, null, null, null
        ));

        String tokenA = jwtService.generateToken(userA.userId(), userA.email());

        // User A tries to access User B's private dashboard
        getMockMvc().perform(get("/api/users/" + userB.userId() + "/dashboard")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("6. GET /api/auth/me returns current authenticated user profile")
    void testAuthMe_Success() throws Exception {
        SignupResponse user = authService.signup(new SignupRequest(
                "Me User", "me." + UUID.randomUUID() + "@example.com", "Password123",
                "Data Scientist", null, null, null, null
        ));

        String token = jwtService.generateToken(user.userId(), user.email());

        getMockMvc().perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Me User"))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.targetCareer").value("Data Scientist"));
    }
}
