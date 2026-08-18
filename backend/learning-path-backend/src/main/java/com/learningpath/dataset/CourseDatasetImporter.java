package com.learningpath.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseDatasetImporter {

    private final CourseRepository courseRepository;
    private final SkillRepository skillRepository;
    private final CourseSkillRepository courseSkillRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Transactional
    public ImportSummary importDataset() {
        long startTime = System.currentTimeMillis();
        log.info("[CourseDatasetImporter] Beginning curated course dataset import...");

        long initialCourseCount = courseRepository.count();
        long initialSkillCount = skillRepository.count();
        log.info("[CourseDatasetImporter] Initial database state: {} courses, {} skills", initialCourseCount, initialSkillCount);

        List<RawCourseRecord> rawRecords = loadRawRecords();
        log.info("[CourseDatasetImporter] Loaded {} raw records from dataset source", rawRecords.size());

        int totalSourceRows = rawRecords.size();
        int validRows = 0;
        int importedCourses = 0;
        int skippedDuplicates = 0;
        int invalidRows = 0;
        int malformedUrls = 0;
        int missingRequiredFields = 0;
        int duplicateCourseCodes = 0;
        int duplicateTitles = 0;

        Set<String> seenCourseCodes = new HashSet<>();
        Set<String> seenTitles = new HashSet<>();
        Set<String> unresolvedSkillsSet = new TreeSet<>();
        List<ImportSummary.RejectedRowDetail> rejectedRows = new ArrayList<>();

        // Cache existing skills by name (case-insensitive)
        Map<String, Skill> existingSkills = new HashMap<>();
        for (Skill s : skillRepository.findAll()) {
            existingSkills.put(s.getName().trim().toLowerCase(), s);
        }

        int rowNum = 1;
        for (RawCourseRecord record : rawRecords) {
            rowNum++;

            // 1. Validate required fields
            if (record.courseId() == null || record.courseId().isBlank()
                    || record.title() == null || record.title().isBlank()
                    || record.platform() == null || record.platform().isBlank()
                    || record.level() == null || record.level().isBlank()
                    || record.link() == null || record.link().isBlank()) {
                missingRequiredFields++;
                invalidRows++;
                rejectedRows.add(new ImportSummary.RejectedRowDetail(rowNum, record.courseId(), record.title(), "Missing one or more required fields"));
                continue;
            }

            // 2. Validate URL syntax
            if (!isValidUrl(record.link())) {
                malformedUrls++;
                invalidRows++;
                rejectedRows.add(new ImportSummary.RejectedRowDetail(rowNum, record.courseId(), record.title(), "Malformed URL: " + record.link()));
                continue;
            }

            // 3. Track in-batch duplicates
            if (!seenCourseCodes.add(record.courseId())) {
                duplicateCourseCodes++;
            }
            if (!seenTitles.add(record.title())) {
                duplicateTitles++;
            }

            // 4. Check for database duplicate course_code (Idempotency)
            if (courseRepository.existsByCourseCode(record.courseId())) {
                skippedDuplicates++;
                validRows++;
                String skillTag = record.skillTag() != null ? record.skillTag().trim() : null;
                if (skillTag != null && !skillTag.isEmpty() && !existingSkills.containsKey(skillTag.toLowerCase())) {
                    unresolvedSkillsSet.add(skillTag);
                }
                continue;
            }

            // 5. Build and persist Course entity
            CourseDifficulty difficulty = CourseDifficulty.fromDatasetLevel(record.level());
            CourseType courseType = CourseType.fromPlatform(record.platform());
            Double durationHours = record.durationHours() != null && record.durationHours() > 0 ? record.durationHours() : 3.0;

            String description = String.format("%s - %s curriculum resource provided by %s for skill '%s'.",
                    record.title(), record.level(), record.platform(), record.skillTag());

            Course course = Course.builder()
                    .courseCode(record.courseId().trim())
                    .title(record.title().trim())
                    .description(description)
                    .provider(record.platform().trim())
                    .url(record.link().trim())
                    .difficulty(difficulty)
                    .durationHours(durationHours)
                    .courseType(courseType)
                    .language("English")
                    .rating(new BigDecimal("4.80"))
                    .price(BigDecimal.ZERO)
                    .isFree(true)
                    .build();

            Course savedCourse = courseRepository.save(course);
            importedCourses++;
            validRows++;

            // 6. Safe Course-Skill Association (Without creating fake skills)
            String skillTag = record.skillTag() != null ? record.skillTag().trim() : null;
            if (skillTag != null && !skillTag.isEmpty()) {
                Skill skill = existingSkills.get(skillTag.toLowerCase());
                if (skill != null) {
                    // Skill exists: map CourseSkill safely
                    if (!courseSkillRepository.existsByCourseIdAndSkillId(savedCourse.getId(), skill.getId())) {
                        CourseSkill courseSkill = CourseSkill.builder()
                                .course(savedCourse)
                                .skill(skill)
                                .coverageLevel(CoverageLevel.INTERMEDIATE)
                                .importance(SkillPriority.HIGH)
                                .targetProficiency(mapProficiencyFromDifficulty(difficulty))
                                .isPrimarySkill(true)
                                .build();
                        courseSkillRepository.save(courseSkill);
                    }
                } else {
                    // Skill not yet mapped: record as unresolved for Step 4
                    unresolvedSkillsSet.add(skillTag);
                }
            }
        }

        long finalCourseCount = courseRepository.count();
        long finalSkillCount = skillRepository.count();
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("[CourseDatasetImporter] Import finished in {} ms. Valid: {}, Imported: {}, Skipped Duplicates: {}, Invalid: {}, Final DB Courses: {}",
                executionTime, validRows, importedCourses, skippedDuplicates, invalidRows, finalCourseCount);

        return new ImportSummary(
                totalSourceRows,
                validRows,
                importedCourses,
                skippedDuplicates,
                invalidRows,
                malformedUrls,
                missingRequiredFields,
                duplicateCourseCodes,
                duplicateTitles,
                new ArrayList<>(unresolvedSkillsSet),
                (int) initialCourseCount,
                finalCourseCount,
                finalSkillCount,
                executionTime,
                rejectedRows
        );
    }

    private List<RawCourseRecord> loadRawRecords() {
        // Priority 1: Direct Excel file from filesystem
        File[] candidateFiles = new File[]{
                new File("datasets/techbot.xlsx"),
                new File("../datasets/techbot.xlsx"),
                new File("datasets/techbot..xlsx"),
                new File("../datasets/techbot..xlsx")
        };

        for (File file : candidateFiles) {
            if (file.exists() && file.isFile() && file.length() > 0) {
                try {
                    log.info("[CourseDatasetImporter] Reading from Excel file: {}", file.getAbsolutePath());
                    return parseExcelFile(file);
                } catch (Exception e) {
                    log.warn("[CourseDatasetImporter] Failed to parse Excel file {}, falling back: {}", file.getName(), e.getMessage());
                }
            }
        }

        // Priority 2: Embedded Classpath JSON Resource
        try {
            Resource jsonResource = resourceLoader.getResource("classpath:data/techbot_courses.json");
            if (jsonResource.exists()) {
                log.info("[CourseDatasetImporter] Reading from classpath:data/techbot_courses.json");
                try (InputStream is = jsonResource.getInputStream()) {
                    return objectMapper.readValue(is, new TypeReference<List<RawCourseRecord>>() {});
                }
            }
        } catch (Exception e) {
            log.error("[CourseDatasetImporter] Failed to read classpath JSON: {}", e.getMessage());
        }

        log.error("[CourseDatasetImporter] No course dataset file could be located!");
        return Collections.emptyList();
    }

    private List<RawCourseRecord> parseExcelFile(File file) throws Exception {
        List<RawCourseRecord> records = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Courses");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }

            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return records;
            }

            // Header mapping
            Row headerRow = rowIterator.next();
            Map<String, Integer> colIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                colIndex.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
            }

            int idIdx = colIndex.getOrDefault("course_id", 0);
            int titleIdx = colIndex.getOrDefault("title", 1);
            int skillIdx = colIndex.getOrDefault("skill_tag", 2);
            int levelIdx = colIndex.getOrDefault("level", 3);
            int durIdx = colIndex.getOrDefault("duration_hours", 4);
            int platIdx = colIndex.getOrDefault("platform", 5);
            int linkIdx = colIndex.getOrDefault("link", 6);

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String courseId = getCellStringValue(row.getCell(idIdx));
                if (courseId == null || courseId.isBlank()) continue;

                String title = getCellStringValue(row.getCell(titleIdx));
                String skillTag = getCellStringValue(row.getCell(skillIdx));
                String level = getCellStringValue(row.getCell(levelIdx));
                Double duration = getCellNumericValue(row.getCell(durIdx));
                String platform = getCellStringValue(row.getCell(platIdx));
                String link = getCellStringValue(row.getCell(linkIdx));

                records.add(new RawCourseRecord(courseId, title, skillTag, level, duration, platform, link));
            }
        }
        return records;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Double getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean isValidUrl(String urlStr) {
        if (urlStr == null || urlStr.isBlank()) return false;
        try {
            URI uri = URI.create(urlStr.trim());
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private ProficiencyLevel mapProficiencyFromDifficulty(CourseDifficulty difficulty) {
        if (difficulty == null) return ProficiencyLevel.BEGINNER;
        return switch (difficulty) {
            case BEGINNER, EASY -> ProficiencyLevel.BEGINNER;
            case INTERMEDIATE, MEDIUM -> ProficiencyLevel.INTERMEDIATE;
            case ADVANCED, HIGH -> ProficiencyLevel.ADVANCED;
            default -> ProficiencyLevel.INTERMEDIATE;
        };
    }
}
