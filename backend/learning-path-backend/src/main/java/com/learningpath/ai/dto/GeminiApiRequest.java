package com.learningpath.ai.dto;

import java.util.List;

public record GeminiApiRequest(List<Content> contents) {

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public static GeminiApiRequest simplePrompt(String promptText) {
        return new GeminiApiRequest(List.of(new Content(List.of(new Part(promptText)))));
    }
}
