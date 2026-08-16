package com.learningpath;

import com.learningpath.config.DotenvPropertyInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LearningPathBackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LearningPathBackendApplication.class);
        app.addInitializers(new DotenvPropertyInitializer());
        app.run(args);
    }

}
