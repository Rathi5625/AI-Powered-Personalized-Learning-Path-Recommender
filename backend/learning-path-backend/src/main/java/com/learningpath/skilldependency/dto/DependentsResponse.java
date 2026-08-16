package com.learningpath.skilldependency.dto;

import java.util.List;

public record DependentsResponse(
        String skill,
        boolean found,
        List<String> directDependents,
        List<String> recursiveDependents
) {
    public static DependentsResponse unknown(String skill) {
        return new DependentsResponse(skill, false, List.of(), List.of());
    }
}
