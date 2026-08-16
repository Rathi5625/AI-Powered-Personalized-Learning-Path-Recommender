package com.learningpath.skilldependency.dto;

import java.util.List;

public record MissingPrerequisitesResponse(
        boolean success,
        List<String> missingPrerequisites,
        List<String> satisfiedPrerequisites,
        List<String> unknownSkills,
        String error
) {
    public static MissingPrerequisitesResponse ok(
            List<String> missingPrerequisites,
            List<String> satisfiedPrerequisites,
            List<String> unknownSkills
    ) {
        return new MissingPrerequisitesResponse(true, missingPrerequisites, satisfiedPrerequisites, unknownSkills, null);
    }

    public static MissingPrerequisitesResponse fail(String error) {
        return new MissingPrerequisitesResponse(false, List.of(), List.of(), List.of(), error);
    }
}
