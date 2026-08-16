package com.learningpath.learningpath.dto;

import java.util.List;
import java.util.UUID;

public record RecommendedCourseItem(
        UUID courseId,
        String courseTitle,
        String provider,
        Double score,
        String difficulty,
        List<String> skillsCovered
) {}
