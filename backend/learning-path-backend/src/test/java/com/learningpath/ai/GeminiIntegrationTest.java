package com.learningpath.ai;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiTestResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeminiIntegrationTest {

    @Autowired
    private GeminiClient geminiClient;

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testLiveGeminiConnectivity() {
        AiTestResponse response = geminiClient.testConnectivity();

        assertNotNull(response, "Response should not be null");
        assertTrue(response.success(), "Gemini API call should succeed when real API key is configured. Error: " + response.error());
        assertNotNull(response.response(), "Response text should not be null");
        assertFalse(response.response().isEmpty(), "Response text should not be empty");
    }
}
