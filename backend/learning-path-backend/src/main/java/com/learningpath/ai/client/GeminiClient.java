package com.learningpath.ai.client;

import com.learningpath.ai.config.GeminiConfig;
import com.learningpath.ai.dto.AiTestResponse;
import com.learningpath.ai.dto.GeminiApiRequest;
import com.learningpath.ai.dto.GeminiApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {

    private final RestClient geminiRestClient;
    private final GeminiConfig geminiConfig;

    public static final String TEST_PROMPT = "Respond with exactly: Gemini integration successful.";

    public AiTestResponse testConnectivity() {
        return generateContent(TEST_PROMPT);
    }

    public AiTestResponse generateContent(String promptText) {
        String model = geminiConfig.getModel();
        String apiKey = geminiConfig.getApiKey();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Gemini Client invocation skipped: API key is not configured.");
            return AiTestResponse.fail(model, "Gemini API key is not configured");
        }

        try {
            String endpointUri = String.format("%s/models/%s:generateContent?key=%s",
                    geminiConfig.getApiUrl(),
                    model,
                    apiKey);

            GeminiApiRequest requestPayload = GeminiApiRequest.simplePrompt(promptText);

            GeminiApiResponse response = geminiRestClient.post()
                    .uri(endpointUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(GeminiApiResponse.class);

            if (response == null) {
                log.warn("Gemini API returned null response body.");
                return AiTestResponse.fail(model, "Empty response received from AI service");
            }

            String extractedText = response.extractFirstText();
            if (extractedText == null || extractedText.trim().isEmpty()) {
                log.warn("Gemini API response contained no candidate text.");
                return AiTestResponse.fail(model, "Malformed or empty text candidate from AI service");
            }

            return AiTestResponse.ok(model, extractedText.trim());

        } catch (ResourceAccessException e) {
            log.error("Gemini API request timed out or network error occurred: {}", e.getMessage());
            return AiTestResponse.fail(model, "AI service request timed out or network unavailable");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Gemini API HTTP Error [Status: {}]: {}", e.getStatusCode(), e.getStatusText());
            return AiTestResponse.fail(model, "AI service temporarily unavailable (HTTP " + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.error("Unexpected error during Gemini API invocation: {}", e.getMessage());
            return AiTestResponse.fail(model, "AI service temporarily unavailable");
        }
    }
}
