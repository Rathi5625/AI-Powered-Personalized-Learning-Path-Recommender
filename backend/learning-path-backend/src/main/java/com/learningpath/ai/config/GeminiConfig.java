package com.learningpath.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@Slf4j
@Getter
public class GeminiConfig {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta}")
    private String apiUrl;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.timeout.seconds:10}")
    private int timeoutSeconds;

    @PostConstruct
    public void validateConfig() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn(
                    "[GeminiConfig] Gemini API key is not configured. Generative AI integration is disabled or operating in fallback mode.");
        } else {
            log.info("[GeminiConfig] Gemini API integration initialized with model: {}", model);
        }
    }

    @Bean
    public RestClient geminiRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) Duration.ofSeconds(timeoutSeconds).toMillis();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
