package com.learningpath.ai.client;

import com.learningpath.ai.config.GeminiConfig;
import com.learningpath.ai.dto.AiTestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class GeminiClientTest {

    @Mock
    private GeminiConfig geminiConfig;

    private RestClient restClient;
    private MockRestServiceServer mockServer;
    private GeminiClient geminiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();

        geminiClient = new GeminiClient(restClient, geminiConfig);
    }

    @Test
    void testMissingApiKey_ReturnsControlledFailure() {
        when(geminiConfig.getApiKey()).thenReturn("");
        when(geminiConfig.getModel()).thenReturn("gemini-1.5-flash");

        AiTestResponse response = geminiClient.testConnectivity();

        assertFalse(response.success());
        assertEquals("gemini-1.5-flash", response.model());
        assertNull(response.response());
        assertEquals("Gemini API key is not configured", response.error());
    }

    @Test
    void testSuccessfulResponse_ParsesTextCorrectly() {
        when(geminiConfig.getApiKey()).thenReturn("dummy-key-123");
        when(geminiConfig.getApiUrl()).thenReturn("https://generativelanguage.googleapis.com/v1beta");
        when(geminiConfig.getModel()).thenReturn("gemini-1.5-flash");

        String responseJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Gemini integration successful."
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=dummy-key-123"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AiTestResponse response = geminiClient.testConnectivity();

        mockServer.verify();
        assertTrue(response.success());
        assertEquals("gemini-1.5-flash", response.model());
        assertEquals("Gemini integration successful.", response.response());
        assertNull(response.error());
    }

    @Test
    void testHttpError_ReturnsControlledFailureWithoutLeakingSecret() {
        when(geminiConfig.getApiKey()).thenReturn("secret-key-xyz");
        when(geminiConfig.getApiUrl()).thenReturn("https://generativelanguage.googleapis.com/v1beta");
        when(geminiConfig.getModel()).thenReturn("gemini-1.5-flash");

        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=secret-key-xyz"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withBadRequest());

        AiTestResponse response = geminiClient.testConnectivity();

        mockServer.verify();
        assertFalse(response.success());
        assertEquals("gemini-1.5-flash", response.model());
        assertNull(response.response());
        assertTrue(response.error().contains("HTTP 400"));
        assertFalse(response.error().contains("secret-key-xyz"));
    }

    @Test
    void testTimeoutError_ReturnsControlledTimeoutFailure() {
        when(geminiConfig.getApiKey()).thenReturn("dummy-key");
        when(geminiConfig.getApiUrl()).thenReturn("https://generativelanguage.googleapis.com/v1beta");
        when(geminiConfig.getModel()).thenReturn("gemini-1.5-flash");

        mockServer.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=dummy-key"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        AiTestResponse response = geminiClient.testConnectivity();

        mockServer.verify();
        assertFalse(response.success());
        assertEquals("gemini-1.5-flash", response.model());
        assertNull(response.response());
        assertTrue(response.error().contains("timed out"));
    }
}
