package com.learningpath.ai.service;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private GeminiClient geminiClient;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(geminiClient);
    }

    @Test
    void testExecuteOperation_Success() {
        AiContext context = new AiContext(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Java Backend Developer",
                "Master Spring Boot",
                "VIDEO",
                10.0,
                List.of("Java"),
                List.of("Spring Boot"),
                List.of("Mastering Spring Boot")
        );

        AiRequest request = new AiRequest(AiOperation.EXPLANATION, context);

        when(geminiClient.generateContent(anyString()))
                .thenReturn(AiTestResponse.ok("gemini-1.5-flash", "AI Service Architecture active for EXPLANATION operation (Java Backend Developer)."));

        AiResponse response = aiService.executeOperation(request);

        assertTrue(response.success());
        assertEquals(AiOperation.EXPLANATION, response.operation());
        assertEquals("gemini-1.5-flash", response.model());
        assertNotNull(response.content());
        assertNull(response.error());
    }

    @Test
    void testExecuteOperation_NullRequest_ReturnsFailureResponse() {
        AiResponse response = aiService.executeOperation(null);

        assertFalse(response.success());
        assertNull(response.operation());
        assertNull(response.content());
        assertEquals("Invalid AI request: operation is required", response.error());
    }

    @Test
    void testExecuteOperation_GeminiClientFailure_ReturnsFailureResponse() {
        AiRequest request = new AiRequest(AiOperation.LEARNING_PATH, null);

        when(geminiClient.generateContent(anyString()))
                .thenReturn(AiTestResponse.fail("gemini-1.5-flash", "Gemini API key is not configured"));

        AiResponse response = aiService.executeOperation(request);

        assertFalse(response.success());
        assertEquals(AiOperation.LEARNING_PATH, response.operation());
        assertEquals("gemini-1.5-flash", response.model());
        assertNull(response.content());
        assertEquals("Gemini API key is not configured", response.error());
    }

    @Test
    void testExecuteOperation_GeminiClientException_ReturnsGenericFailureWithoutLeaks() {
        AiRequest request = new AiRequest(AiOperation.SUMMARY, null);

        when(geminiClient.generateContent(anyString()))
                .thenThrow(new RuntimeException("Internal client exception with sensitive-key-123"));

        AiResponse response = aiService.executeOperation(request);

        assertFalse(response.success());
        assertEquals(AiOperation.SUMMARY, response.operation());
        assertNull(response.content());
        assertEquals("AI service temporarily unavailable", response.error());
        assertFalse(response.error().contains("sensitive-key-123"));
    }
}
