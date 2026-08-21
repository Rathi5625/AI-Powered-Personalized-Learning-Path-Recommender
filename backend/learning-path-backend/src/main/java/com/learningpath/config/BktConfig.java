package com.learningpath.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bkt")
public class BktConfig {
    private double initialKnowledge = 0.20; // P(L0)
    private double learnProbability = 0.15;  // P(T)
    private double guessProbability = 0.20;  // P(G)
    private double slipProbability = 0.10;   // P(S)

    private double masteryThreshold = 0.85;
    private double proficientThreshold = 0.70;
    private double basicThreshold = 0.50;
    private double developingThreshold = 0.30;
}
