package com.learningpath.ai.service;

import com.learningpath.ai.client.GeminiClient;
import com.learningpath.ai.dto.AiOperation;
import com.learningpath.ai.dto.AiRequest;
import com.learningpath.ai.dto.AiResponse;
import com.learningpath.ai.dto.AiTestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final GeminiClient geminiClient;

    public AiResponse executeOperation(AiRequest request) {
        if (request == null || request.operation() == null) {
            log.warn("[AiService] Invalid AI request: missing request or operation.");
            return AiResponse.fail(null, null, "Invalid AI request: operation is required");
        }

        AiOperation operation = request.operation();
        String careerName = (request.context() != null && request.context().careerName() != null)
                ? request.context().careerName()
                : "Software Engineering";

        log.info("[AiService] Processing operation: {} for target context: {}", operation, careerName);

        // Architecture verification prompt for Step 10B
        String promptText = String.format("Respond with exactly: AI Service Architecture active for %s operation (%s).",
                operation, careerName);

        try {
            AiTestResponse clientResponse = geminiClient.generateContent(promptText);
            if (clientResponse.success()) {
                return AiResponse.ok(operation, clientResponse.model(), clientResponse.response());
            } else {
                log.warn("[AiService] GeminiClient returned failure for operation {}: {}", operation, clientResponse.error());
                return AiResponse.fail(operation, clientResponse.model(), clientResponse.error());
            }
        } catch (Exception e) {
            log.error("[AiService] Exception occurred during operation execution: {}", e.getMessage());
            return AiResponse.fail(operation, null, "AI service temporarily unavailable");
        }
    }
}
