package com.learningpath.recommendation.client;

import com.learningpath.recommendation.dto.MlPredictionRequest;
import com.learningpath.recommendation.dto.MlPredictionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MlRecommendationClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private MlRecommendationClient mlClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mlClient = new MlRecommendationClient(restClient);
    }

    @Test
    void testPredictSuccess() {
        MlPredictionRequest request = MlPredictionRequest.builder()
                .skillGapScore(0.9)
                .careerPriorityScore(1.0)
                .build();

        MlPredictionResponse mockResponse = new MlPredictionResponse(0.95, 95.0, true, "1.0");

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/predict")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(MlPredictionRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(MlPredictionResponse.class)).thenReturn(mockResponse);

        Optional<MlPredictionResponse> result = mlClient.predict(request);

        assertTrue(result.isPresent());
        assertEquals(95.0, result.get().recommendationScore());
        assertTrue(result.get().recommended());
    }

    @Test
    void testPredictFallbackOnException() {
        MlPredictionRequest request = MlPredictionRequest.builder().build();

        when(restClient.post()).thenThrow(new RestClientException("Connection refused"));

        Optional<MlPredictionResponse> result = mlClient.predict(request);

        assertTrue(result.isEmpty(), "Should return empty Optional on error to enable fallback");
    }
}
