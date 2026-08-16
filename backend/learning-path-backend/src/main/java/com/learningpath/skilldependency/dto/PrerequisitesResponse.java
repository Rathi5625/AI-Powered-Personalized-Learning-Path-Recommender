package com.learningpath.skilldependency.dto;

import java.util.List;

public record PrerequisitesResponse(
        String skill,
        boolean found,
        List<String> directPrerequisites,
        List<String> recursivePrerequisites
) {
    public static PrerequisitesResponse unknown(String skill) {
        return new PrerequisitesResponse(skill, false, List.of(), List.of());
    }
}
