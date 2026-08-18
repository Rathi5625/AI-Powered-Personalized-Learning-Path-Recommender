package com.learningpath.entity.enums;

public enum CourseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ALL_LEVELS,
    EASY,
    MEDIUM,
    HIGH;

    public static CourseDifficulty fromDatasetLevel(String level) {
        if (level == null || level.trim().isEmpty()) {
            return BEGINNER;
        }
        return switch (level.trim().toUpperCase()) {
            case "BEGINNER" -> BEGINNER;
            case "EASY" -> EASY;
            case "MEDIUM" -> MEDIUM;
            case "HIGH", "HARD" -> HIGH;
            case "INTERMEDIATE" -> INTERMEDIATE;
            case "ADVANCED" -> ADVANCED;
            case "ALL_LEVELS", "ALL" -> ALL_LEVELS;
            default -> BEGINNER;
        };
    }
}
