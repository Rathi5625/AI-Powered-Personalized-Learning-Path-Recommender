package com.learningpath.ai.controller;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiOperation;
import com.learningpath.ai.dto.AiResponse;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.service.AiService;
import com.learningpath.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AiService aiService;

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private AiController aiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(aiController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetGeminiTestEndpoint_Success() throws Exception {
        when(geminiClient.testConnectivity())
                .thenReturn(AiTestResponse.ok("gemini-1.5-flash", "Gemini integration successful."));

        mockMvc.perform(get("/api/ai/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.model").value("gemini-1.5-flash"))
                .andExpect(jsonPath("$.response").value("Gemini integration successful."));
    }

    @Test
    void testTestArchitectureEndpoint_Success() throws Exception {
        String json = """
                {
                  "operation": "EXPLANATION",
                  "context": {
                    "careerName": "Java Backend Developer"
                  }
                }
                """;

        when(aiService.executeOperation(any()))
                .thenReturn(AiResponse.ok(AiOperation.EXPLANATION, "gemini-1.5-flash", "Architecture validation successful."));

        mockMvc.perform(post("/api/ai/test-architecture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.operation").value("EXPLANATION"))
                .andExpect(jsonPath("$.content").value("Architecture validation successful."))
                .andExpect(jsonPath("$.model").value("gemini-1.5-flash"));
    }

    @Test
    void testTestArchitectureEndpoint_MissingOperation_ShouldReturn400BadRequest() throws Exception {
        String invalidJson = """
                {
                  "context": {
                    "careerName": "Java Backend Developer"
                  }
                }
                """;

        mockMvc.perform(post("/api/ai/test-architecture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
