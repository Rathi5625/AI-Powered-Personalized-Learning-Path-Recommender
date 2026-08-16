package com.learningpath.ai.controller;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiRequest;
import com.learningpath.ai.dto.AiResponse;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Generative AI operations and development architecture verification.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final GeminiClient geminiClient;

    /**
     * Development endpoint for verifying direct Gemini API connectivity (Step 10A).
     */
    @GetMapping("/test")
    public ResponseEntity<AiTestResponse> testGeminiConnectivity() {
        AiTestResponse response = geminiClient.testConnectivity();
        return ResponseEntity.ok(response);
    }

    /**
     * Development architecture verification endpoint (Step 10B).
     * Verifies call flow: AiController -> AiService -> GeminiClient -> Gemini API.
     */
    @PostMapping("/test-architecture")
    public ResponseEntity<AiResponse> testArchitecture(@Valid @RequestBody AiRequest request) {
        AiResponse response = aiService.executeOperation(request);
        return ResponseEntity.ok(response);
    }
}
