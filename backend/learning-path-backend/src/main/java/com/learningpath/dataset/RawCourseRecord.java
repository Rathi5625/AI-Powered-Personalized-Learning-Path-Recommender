package com.learningpath.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawCourseRecord(
        @JsonProperty("course_id")
        String courseId,

        @JsonProperty("title")
        String title,

        @JsonProperty("skill_tag")
        String skillTag,

        @JsonProperty("level")
        String level,

        @JsonProperty("duration_hours")
        Double durationHours,

        @JsonProperty("platform")
        String platform,

        @JsonProperty("link")
        String link
) {
}
