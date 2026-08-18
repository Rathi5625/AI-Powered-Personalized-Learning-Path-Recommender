package com.learningpath.dataset;

import java.util.ArrayList;
import java.util.List;

public record ImportSummary(
        int totalSourceRows,
        int validRows,
        int importedCourses,
        int skippedDuplicates,
        int invalidRows,
        int malformedUrls,
        int missingRequiredFields,
        int duplicateCourseCodes,
        int duplicateTitles,
        List<String> unresolvedSkills,
        int existingCoursesPreserved,
        long finalDatabaseCourseCount,
        long finalDatabaseSkillCount,
        long executionTimeMs,
        List<RejectedRowDetail> rejectedRows
) {
    public static ImportSummary empty() {
        return new ImportSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, List.of(), 0, 0, 0, 0, List.of());
    }

    public record RejectedRowDetail(
            int rowNumber,
            String courseCode,
            String title,
            String reason
    ) {}
}
