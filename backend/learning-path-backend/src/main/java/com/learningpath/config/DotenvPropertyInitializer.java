package com.learningpath.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DotenvPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        File envFile = findDotenvFile();
        if (envFile == null || !envFile.exists()) {
            log.info("[DotenvPropertyInitializer] No local .env file found. Using system environment variables.");
            return;
        }

        try {
            Map<String, Object> envProperties = parseEnvFile(envFile);
            if (!envProperties.isEmpty()) {
                // Populate system properties so Spring ${PLACEHOLDER} resolution and JavaMailSender resolve properly
                for (Map.Entry<String, Object> entry : envProperties.entrySet()) {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue().toString());
                    }
                }
                environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envProperties));
                log.info("[DotenvPropertyInitializer] Successfully loaded {} environment variables from {}",
                        envProperties.size(), envFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("[DotenvPropertyInitializer] Could not parse .env file: {}", e.getMessage());
        }
    }

    private File findDotenvFile() {
        String userDir = System.getProperty("user.dir", ".");
        File[] candidates = new File[] {
                new File(".env"),
                new File("backend/learning-path-backend/.env"),
                new File("../.env"),
                new File(userDir, ".env"),
                new File(userDir, "backend/learning-path-backend/.env"),
                new File(userDir, "../.env")
        };

        for (File candidate : candidates) {
            try {
                if (candidate.exists() && candidate.isFile()) {
                    return candidate.getCanonicalFile();
                }
            } catch (IOException ignored) {
                if (candidate.exists() && candidate.isFile()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private Map<String, Object> parseEnvFile(File envFile) throws IOException {
        Map<String, Object> props = new HashMap<>();
        List<String> lines = Files.readAllLines(envFile.toPath());

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            int eqIdx = trimmed.indexOf('=');
            if (eqIdx > 0) {
                String key = trimmed.substring(0, eqIdx).trim();
                String val = trimmed.substring(eqIdx + 1).trim();

                // Strip quotes if present
                if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }

                if (!key.isEmpty()) {
                    props.put(key, val);
                }
            }
        }
        return props;
    }
}
