package com.learningpath.config;

import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillDifficulty;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class CourseDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final SkillRepository skillRepository;
    private final CourseSkillRepository courseSkillRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            log.info("Courses already initialized. Skipping seed data insertion.");
            return;
        }

        log.info("Initializing realistic seed data for Courses and Course Skills...");

        // Ensure skills catalog exists
        Map<String, Skill> skillMap = ensureSkillsCatalog();

        // Seed 21 realistic courses
        seedCoursesAndMappings(skillMap);

        log.info("Course catalog and CourseSkill mappings successfully populated.");
    }

    private Map<String, Skill> ensureSkillsCatalog() {
        Map<String, Skill> skillMap = skillRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Skill::getName, s -> s, (e, r) -> e));

        if (!skillMap.isEmpty()) {
            return skillMap;
        }

        log.info("Seeding skills catalog for courses...");
        Map<String, SkillDefinition> defs = Map.ofEntries(
                Map.entry("Java", new SkillDefinition("Programming", "Core Java language, syntax, OOP & JVM internals", SkillDifficulty.INTERMEDIATE)),
                Map.entry("OOP", new SkillDefinition("Programming", "Object-Oriented Programming principles & design patterns", SkillDifficulty.BEGINNER)),
                Map.entry("Data Structures & Algorithms", new SkillDefinition("Computer Science", "Arrays, Trees, Graphs, Sorting & Searching algorithms", SkillDifficulty.INTERMEDIATE)),
                Map.entry("SQL", new SkillDefinition("Database", "Relational Database queries, indexing & transactions", SkillDifficulty.BEGINNER)),
                Map.entry("Spring Boot", new SkillDefinition("Backend Framework", "Spring Boot auto-config, dependency injection & REST services", SkillDifficulty.INTERMEDIATE)),
                Map.entry("REST APIs", new SkillDefinition("Web Services", "REST architectural style, HTTP methods & API design", SkillDifficulty.BEGINNER)),
                Map.entry("JPA/Hibernate", new SkillDefinition("Database", "Object-Relational Mapping, entity lifecycle & JPQL", SkillDifficulty.INTERMEDIATE)),
                Map.entry("Spring Security", new SkillDefinition("Security", "Authentication, authorization, OAuth2 & JWT tokens", SkillDifficulty.ADVANCED)),
                Map.entry("Testing", new SkillDefinition("DevOps & QA", "JUnit 5, Mockito, unit & integration testing", SkillDifficulty.BEGINNER)),
                Map.entry("Docker", new SkillDefinition("DevOps", "Containerization, Dockerfiles, compose & container networking", SkillDifficulty.INTERMEDIATE)),
                Map.entry("HTML", new SkillDefinition("Frontend", "HyperText Markup Language & semantic web markup", SkillDifficulty.BEGINNER)),
                Map.entry("CSS", new SkillDefinition("Frontend", "Cascading Style Sheets, Flexbox, Grid & responsive design", SkillDifficulty.BEGINNER)),
                Map.entry("JavaScript", new SkillDefinition("Frontend", "Modern ECMAScript, async/await, DOM manipulation", SkillDifficulty.BEGINNER)),
                Map.entry("TypeScript", new SkillDefinition("Frontend", "Static typing for JavaScript & advanced type definitions", SkillDifficulty.INTERMEDIATE)),
                Map.entry("React", new SkillDefinition("Frontend Framework", "React components, state management, hooks & virtual DOM", SkillDifficulty.INTERMEDIATE)),
                Map.entry("Git", new SkillDefinition("Tools", "Version control system, branching strategies & pull requests", SkillDifficulty.BEGINNER)),
                Map.entry("Python", new SkillDefinition("Programming", "Python language syntax, scripting & data structures", SkillDifficulty.BEGINNER)),
                Map.entry("Pandas", new SkillDefinition("Data Science", "Data manipulation & analysis library", SkillDifficulty.INTERMEDIATE)),
                Map.entry("NumPy", new SkillDefinition("Data Science", "Numerical computing & vector arrays library", SkillDifficulty.BEGINNER)),
                Map.entry("Statistics", new SkillDefinition("Mathematics", "Probability distributions, hypothesis testing & regression", SkillDifficulty.INTERMEDIATE)),
                Map.entry("Data Visualization", new SkillDefinition("Data Science", "Matplotlib, Seaborn & charting frameworks", SkillDifficulty.BEGINNER)),
                Map.entry("Machine Learning", new SkillDefinition("AI/ML", "Supervised/unsupervised algorithms, Scikit-learn", SkillDifficulty.INTERMEDIATE)),
                Map.entry("Deep Learning", new SkillDefinition("AI/ML", "Neural networks, CNNs, RNNs & Transformers", SkillDifficulty.ADVANCED)),
                Map.entry("TensorFlow/PyTorch", new SkillDefinition("AI/ML Framework", "Deep learning model design, training & evaluation", SkillDifficulty.ADVANCED)),
                Map.entry("MLOps", new SkillDefinition("DevOps & ML", "Machine learning pipeline deployment, monitoring & tracking", SkillDifficulty.ADVANCED))
        );

        Map<String, Skill> resultMap = new HashMap<>();
        defs.forEach((name, def) -> {
            Skill skill = skillRepository.findByName(name)
                    .orElseGet(() -> skillRepository.save(Skill.builder()
                            .name(name)
                            .category(def.category)
                            .description(def.description)
                            .difficulty(def.difficulty)
                            .build()));
            resultMap.put(name, skill);
        });
        return resultMap;
    }

    private void seedCoursesAndMappings(Map<String, Skill> skills) {
        // 1. Java Programming Fundamentals
        createCourseWithSkills(
                "Java Programming Fundamentals",
                "Comprehensive introduction to core Java syntax, data types, control flow, and methods.",
                "Coursera",
                "https://example.org/courses/java-fundamentals",
                CourseDifficulty.BEGINNER,
                25.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.80"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("Java"), CoverageLevel.BASIC, SkillPriority.CRITICAL, ProficiencyLevel.BEGINNER, true),
                new SkillMapping(skills.get("OOP"), CoverageLevel.BASIC, SkillPriority.HIGH, ProficiencyLevel.BEGINNER, false)
        );

        // 2. Object-Oriented Programming with Java
        createCourseWithSkills(
                "Object-Oriented Programming with Java",
                "Master encapsulation, inheritance, polymorphism, abstraction, and OOP design patterns.",
                "edX",
                "https://example.org/courses/java-oop",
                CourseDifficulty.INTERMEDIATE,
                20.0,
                CourseType.INTERACTIVE_COURSE,
                "English",
                new BigDecimal("4.75"),
                new BigDecimal("49.99"),
                false,
                new SkillMapping(skills.get("OOP"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("Java"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 3. Data Structures and Algorithms in Java
        createCourseWithSkills(
                "Data Structures and Algorithms in Java",
                "Implement stacks, queues, trees, graphs, sorting, searching, and algorithm complexity analysis.",
                "Udemy",
                "https://example.org/courses/java-dsa",
                CourseDifficulty.INTERMEDIATE,
                40.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.90"),
                new BigDecimal("89.99"),
                false,
                new SkillMapping(skills.get("Data Structures & Algorithms"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("Java"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 4. SQL and Relational Database Fundamentals
        createCourseWithSkills(
                "SQL and Relational Database Fundamentals",
                "Learn relational database design, SQL queries, joins, indexes, subqueries, and transactions.",
                "Khan Academy",
                "https://example.org/courses/sql-fundamentals",
                CourseDifficulty.BEGINNER,
                15.0,
                CourseType.INTERACTIVE_COURSE,
                "English",
                new BigDecimal("4.85"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("SQL"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true)
        );

        // 5. Spring Boot 3 Fundamentals
        createCourseWithSkills(
                "Spring Boot 3 Fundamentals",
                "Build modern Java web applications with Spring Boot 3, dependency injection, and auto-configuration.",
                "Pluralsight",
                "https://example.org/courses/spring-boot-3",
                CourseDifficulty.INTERMEDIATE,
                18.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.88"),
                new BigDecimal("29.99"),
                false,
                new SkillMapping(skills.get("Spring Boot"), CoverageLevel.BASIC, SkillPriority.CRITICAL, ProficiencyLevel.BEGINNER, true),
                new SkillMapping(skills.get("Java"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 6. Building Production REST APIs with Spring Boot
        createCourseWithSkills(
                "Building Production REST APIs with Spring Boot",
                "Design and implement RESTful endpoints, DTO mappings, validation, error handling, and swagger OpenAPI.",
                "Udemy",
                "https://example.org/courses/spring-boot-rest-api",
                CourseDifficulty.INTERMEDIATE,
                22.0,
                CourseType.PROJECT_BASED,
                "English",
                new BigDecimal("4.92"),
                new BigDecimal("69.99"),
                false,
                new SkillMapping(skills.get("REST APIs"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("Spring Boot"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 7. JPA and Hibernate Deep Dive
        createCourseWithSkills(
                "JPA and Hibernate Deep Dive",
                "Object-relational mapping, entity mappings, relationships, caching, performance tuning, and JPQL.",
                "Baeldung",
                "https://example.org/courses/jpa-hibernate",
                CourseDifficulty.ADVANCED,
                30.0,
                CourseType.TEXT_TUTORIAL,
                "English",
                new BigDecimal("4.80"),
                new BigDecimal("59.99"),
                false,
                new SkillMapping(skills.get("JPA/Hibernate"), CoverageLevel.ADVANCED, SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED, true),
                new SkillMapping(skills.get("SQL"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 8. Mastering Spring Security 6
        createCourseWithSkills(
                "Mastering Spring Security 6",
                "Implement authentication, authorization, role-based security, OAuth2, and JWT token management.",
                "Udemy",
                "https://example.org/courses/spring-security-6",
                CourseDifficulty.ADVANCED,
                28.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.86"),
                new BigDecimal("79.99"),
                false,
                new SkillMapping(skills.get("Spring Security"), CoverageLevel.ADVANCED, SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED, true),
                new SkillMapping(skills.get("Spring Boot"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 9. Automated Testing for Java Developers
        createCourseWithSkills(
                "Automated Testing for Java Developers",
                "Master JUnit 5, Mockito, AssertJ, Integration Testing, and Test-Driven Development (TDD).",
                "Coursera",
                "https://example.org/courses/java-testing",
                CourseDifficulty.INTERMEDIATE,
                16.0,
                CourseType.PROJECT_BASED,
                "English",
                new BigDecimal("4.78"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("Testing"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("Java"), CoverageLevel.INTERMEDIATE, SkillPriority.MEDIUM, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 10. Docker & Containers for Java Developers
        createCourseWithSkills(
                "Docker & Containers for Java Developers",
                "Containerize Java Spring Boot microservices with Dockerfiles, multi-stage builds, and Docker Compose.",
                "Udemy",
                "https://example.org/courses/docker-java",
                CourseDifficulty.INTERMEDIATE,
                14.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.82"),
                new BigDecimal("39.99"),
                false,
                new SkillMapping(skills.get("Docker"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, true)
        );

        // 11. HTML5 and CSS3 Responsive Web Design
        createCourseWithSkills(
                "HTML5 and CSS3 Responsive Web Design",
                "Build modern, mobile-first responsive web pages using Flexbox, CSS Grid, and HTML5 semantics.",
                "freeCodeCamp",
                "https://example.org/courses/html-css-responsive",
                CourseDifficulty.BEGINNER,
                30.0,
                CourseType.INTERACTIVE_COURSE,
                "English",
                new BigDecimal("4.95"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("HTML"), CoverageLevel.ADVANCED, SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED, true),
                new SkillMapping(skills.get("CSS"), CoverageLevel.ADVANCED, SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED, false)
        );

        // 12. Modern JavaScript Essentials
        createCourseWithSkills(
                "Modern JavaScript Essentials",
                "ES6+ syntax, asynchronous JS, Promises, async/await, DOM manipulation, and fetch API.",
                "scrimba",
                "https://example.org/courses/modern-js",
                CourseDifficulty.BEGINNER,
                24.0,
                CourseType.INTERACTIVE_COURSE,
                "English",
                new BigDecimal("4.89"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("JavaScript"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true)
        );

        // 13. TypeScript Fundamentals
        createCourseWithSkills(
                "TypeScript Fundamentals",
                "Add static typing to JavaScript, interfaces, generics, type aliases, and TS compiler configuration.",
                "Frontend Masters",
                "https://example.org/courses/typescript-fundamentals",
                CourseDifficulty.INTERMEDIATE,
                12.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.84"),
                new BigDecimal("35.00"),
                false,
                new SkillMapping(skills.get("TypeScript"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("JavaScript"), CoverageLevel.INTERMEDIATE, SkillPriority.MEDIUM, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 14. React - The Complete Guide
        createCourseWithSkills(
                "React - The Complete Guide",
                "Build modern single-page applications with React components, State, Hooks, Context, and Redux.",
                "Udemy",
                "https://example.org/courses/react-complete-guide",
                CourseDifficulty.INTERMEDIATE,
                48.0,
                CourseType.PROJECT_BASED,
                "English",
                new BigDecimal("4.91"),
                new BigDecimal("94.99"),
                false,
                new SkillMapping(skills.get("React"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("JavaScript"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 15. Git & GitHub Version Control Essentials
        createCourseWithSkills(
                "Git & GitHub Version Control Essentials",
                "Git workflow, commits, branching, merging, pull requests, resolving merge conflicts, and GitHub actions.",
                "YouTube",
                "https://example.org/courses/git-github-essentials",
                CourseDifficulty.BEGINNER,
                8.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.87"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("Git"), CoverageLevel.BASIC, SkillPriority.HIGH, ProficiencyLevel.BEGINNER, true)
        );

        // 16. Python Programming for Beginners
        createCourseWithSkills(
                "Python Programming for Beginners",
                "Learn Python syntax, data structures, file handling, modules, and basic object-oriented programming.",
                "Coursera",
                "https://example.org/courses/python-beginners",
                CourseDifficulty.BEGINNER,
                20.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.83"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("Python"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true)
        );

        // 17. Data Analysis with Pandas & NumPy
        createCourseWithSkills(
                "Data Analysis with Pandas & NumPy",
                "Data cleaning, transformation, aggregation, indexing, and exploratory data analysis in Python.",
                "DataCamp",
                "https://example.org/courses/pandas-numpy-analysis",
                CourseDifficulty.INTERMEDIATE,
                22.0,
                CourseType.INTERACTIVE_COURSE,
                "English",
                new BigDecimal("4.80"),
                new BigDecimal("25.00"),
                false,
                new SkillMapping(skills.get("Pandas"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("NumPy"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false),
                new SkillMapping(skills.get("Python"), CoverageLevel.INTERMEDIATE, SkillPriority.MEDIUM, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 18. Applied Statistics for Data Science
        createCourseWithSkills(
                "Applied Statistics for Data Science",
                "Descriptive statistics, probability, hypothesis testing, A/B testing, and linear regression analysis.",
                "edX",
                "https://example.org/courses/applied-statistics",
                CourseDifficulty.INTERMEDIATE,
                35.0,
                CourseType.VIDEO_COURSE,
                "English",
                new BigDecimal("4.76"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("Statistics"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("Data Visualization"), CoverageLevel.BASIC, SkillPriority.HIGH, ProficiencyLevel.BEGINNER, false)
        );

        // 19. Machine Learning Fundamentals with Scikit-Learn
        createCourseWithSkills(
                "Machine Learning Fundamentals with Scikit-Learn",
                "Supervised algorithms, regression, classification, clustering, model evaluation, and cross-validation.",
                "Kaggle",
                "https://example.org/courses/scikit-learn-ml",
                CourseDifficulty.INTERMEDIATE,
                30.0,
                CourseType.PROJECT_BASED,
                "English",
                new BigDecimal("4.93"),
                BigDecimal.ZERO,
                true,
                new SkillMapping(skills.get("Machine Learning"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("Python"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );

        // 20. Deep Learning & Neural Networks with PyTorch
        createCourseWithSkills(
                "Deep Learning & Neural Networks with PyTorch",
                "Artificial neural networks, CNNs, RNNs, Transformers, model training, and PyTorch tensors.",
                "Udacity",
                "https://example.org/courses/pytorch-deep-learning",
                CourseDifficulty.ADVANCED,
                50.0,
                CourseType.BOOTCAMP,
                "English",
                new BigDecimal("4.89"),
                new BigDecimal("199.00"),
                false,
                new SkillMapping(skills.get("Deep Learning"), CoverageLevel.ADVANCED, SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED, true),
                new SkillMapping(skills.get("TensorFlow/PyTorch"), CoverageLevel.ADVANCED, SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED, false)
        );

        // 21. Production MLOps and Model Deployment
        createCourseWithSkills(
                "Production MLOps and Model Deployment",
                "Build automated ML pipelines, model tracking, containerization with Docker, and monitoring.",
                "Coursera",
                "https://example.org/courses/mlops-deployment",
                CourseDifficulty.ADVANCED,
                36.0,
                CourseType.BOOTCAMP,
                "English",
                new BigDecimal("4.85"),
                new BigDecimal("149.00"),
                false,
                new SkillMapping(skills.get("MLOps"), CoverageLevel.INTERMEDIATE, SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE, true),
                new SkillMapping(skills.get("Docker"), CoverageLevel.INTERMEDIATE, SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE, false)
        );
    }

    private void createCourseWithSkills(
            String title,
            String description,
            String provider,
            String url,
            CourseDifficulty difficulty,
            Double durationHours,
            CourseType courseType,
            String language,
            BigDecimal rating,
            BigDecimal price,
            boolean isFree,
            SkillMapping... mappings
    ) {
        Course course = courseRepository.save(Course.builder()
                .title(title)
                .description(description)
                .provider(provider)
                .url(url)
                .difficulty(difficulty)
                .durationHours(durationHours)
                .durationMinutes((int) (durationHours * 60))
                .courseType(courseType)
                .language(language)
                .rating(rating)
                .price(price)
                .isFree(isFree)
                .build());

        for (SkillMapping sm : mappings) {
            if (sm.skill != null) {
                courseSkillRepository.save(CourseSkill.builder()
                        .course(course)
                        .skill(sm.skill)
                        .coverageLevel(sm.coverageLevel)
                        .importance(sm.importance)
                        .targetProficiency(sm.targetProficiency)
                        .isPrimarySkill(sm.isPrimarySkill)
                        .build());
            }
        }
    }

    private record SkillDefinition(String category, String description, SkillDifficulty difficulty) {}

    private record SkillMapping(
            Skill skill,
            CoverageLevel coverageLevel,
            SkillPriority importance,
            ProficiencyLevel targetProficiency,
            boolean isPrimarySkill
    ) {}
}
