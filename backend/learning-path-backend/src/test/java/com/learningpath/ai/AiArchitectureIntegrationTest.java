package com.learningpath.ai;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiOperation;
import com.learningpath.ai.dto.AiRequest;
import com.learningpath.ai.dto.AiResponse;
import com.learningpath.ai.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static com.learningpath.ai.dto.AiTestResponse.ok;

@SpringBootTest
class AiArchitectureIntegrationTest {

    @Autowired
    private AiService aiService;

    @MockitoBean
    private GeminiClient geminiClient;

    @Test
    void testAiArchitectureFlow_WiresComponentsSuccessfully() {
        when(geminiClient.generateContent(anyString()))
                .thenReturn(ok("gemini-1.5-flash", "AI Service Architecture active for EXPLANATION operation."));

        AiRequest request = new AiRequest(AiOperation.EXPLANATION, null);
        AiResponse response = aiService.executeOperation(request);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.success());
        assertEquals(AiOperation.EXPLANATION, response.operation());
        assertEquals("gemini-1.5-flash", response.model());
        assertNotNull(response.content());
        assertNull(response.error());
    }
}
