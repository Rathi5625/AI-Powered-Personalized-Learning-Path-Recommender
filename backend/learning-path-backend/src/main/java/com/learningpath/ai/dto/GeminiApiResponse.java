package com.learningpath.ai.dto;

import java.util.List;

public record GeminiApiResponse(List<Candidate> candidates) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public String extractFirstText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Candidate candidate = candidates.get(0);
        if (candidate == null || candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
            return null;
        }
        return candidate.content().parts().get(0).text();
    }
}
