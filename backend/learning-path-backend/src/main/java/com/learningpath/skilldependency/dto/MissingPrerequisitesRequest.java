package com.learningpath.skilldependency.dto;

import java.util.List;

public record MissingPrerequisitesRequest(
        List<String> currentSkills,
        List<String> targetSkills
) {}
