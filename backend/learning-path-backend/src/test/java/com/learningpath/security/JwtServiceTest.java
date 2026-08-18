package com.learningpath.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    @DisplayName("Generate and validate valid JWT token")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String email = "test.user@example.com";

        String token = jwtService.generateToken(userId, email);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
    }

    @Test
    @DisplayName("Reject invalid/malformed token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.payload";

        assertThat(jwtService.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("Reject expired token")
    void testValidateExpiredToken() throws InterruptedException {
        // JwtService with 1ms expiration
        JwtService shortLivedJwtService = new JwtService(secret, 1);
        UUID userId = UUID.randomUUID();
        String email = "expired@example.com";

        String token = shortLivedJwtService.generateToken(userId, email);
        Thread.sleep(10); // Wait for token to expire

        assertThat(shortLivedJwtService.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Verify configured expiration time")
    void testGetExpirationMs() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(3600000);
    }
}
