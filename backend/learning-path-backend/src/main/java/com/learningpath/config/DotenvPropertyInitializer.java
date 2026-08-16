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
            return;
        }

        try {
            Map<String, Object> envProperties = parseEnvFile(envFile);
            if (!envProperties.isEmpty()) {
                environment.getPropertySources().addLast(new MapPropertySource("dotenvProperties", envProperties));
                log.info("[DotenvPropertyInitializer] Loaded {} environment variables from {}", envProperties.size(), envFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("[DotenvPropertyInitializer] Could not parse .env file: {}", e.getMessage());
        }
    }

    private File findDotenvFile() {
        File f1 = new File(".env");
        if (f1.exists()) return f1;

        File f2 = new File("backend/learning-path-backend/.env");
        if (f2.exists()) return f2;

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
