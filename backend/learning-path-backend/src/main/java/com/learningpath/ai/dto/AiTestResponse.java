package com.learningpath.ai.dto;

public record AiTestResponse(
        boolean success,
        String model,
        String response,
        String error
) {
    public static AiTestResponse ok(String model, String responseText) {
        return new AiTestResponse(true, model, responseText, null);
    }

    public static AiTestResponse fail(String model, String errorMessage) {
        return new AiTestResponse(false, model, null, errorMessage);
    }
}
