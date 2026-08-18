package com.learningpath.entity.enums;

public enum CourseType {
    VIDEO_COURSE,
    INTERACTIVE_COURSE,
    TEXT_TUTORIAL,
    BOOTCAMP,
    PROJECT_BASED,
    DOCUMENTATION;

    public static CourseType fromPlatform(String platform) {
        if (platform == null || platform.trim().isEmpty()) {
            return TEXT_TUTORIAL;
        }
        String p = platform.trim().toLowerCase();
        if (p.contains("youtube") || p.contains("video") || p.contains("coursera") || p.contains("udemy")) {
            return VIDEO_COURSE;
        } else if (p.contains("interactive") || p.contains("scrimba") || p.contains("codecademy") || p.contains("freecodecamp")) {
            return INTERACTIVE_COURSE;
        } else if (p.contains("mdn") || p.contains("docs") || p.contains("documentation") || p.contains("microsoft") || p.contains("google")) {
            return DOCUMENTATION;
        }
        return TEXT_TUTORIAL;
    }
}
