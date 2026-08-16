package com.learningpath.recommendation.client;

import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MlRecommendationClient {

    private final RestClient mlRestClient;

    public Optional<MlPredictionResponse> predict(MlPredictionRequest request) {
        try {
            MlPredictionResponse response = mlRestClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MlPredictionResponse.class);

            return Optional.ofNullable(response);
        } catch (Exception e) {
            log.warn("Python ML Service unavailable or request failed: {}. Falling back to rule-based recommendation.", e.getMessage());
            return Optional.empty();
        }
    }
}
