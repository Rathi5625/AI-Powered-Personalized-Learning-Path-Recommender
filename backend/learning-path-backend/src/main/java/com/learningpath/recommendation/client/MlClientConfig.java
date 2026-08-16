package com.learningpath.recommendation.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class MlClientConfig {

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    @Bean
    public RestClient mlRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(2).toMillis());

        return RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
