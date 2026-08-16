package com.learningpath.ai.dto;

public record AiResponse(
        boolean success,
        AiOperation operation,
        String content,
        String model,
        String error
) {
    public static AiResponse ok(AiOperation operation, String model, String content) {
        return new AiResponse(true, operation, content, model, null);
    }

    public static AiResponse fail(AiOperation operation, String model, String errorMessage) {
        return new AiResponse(false, operation, null, model, errorMessage);
    }
}
