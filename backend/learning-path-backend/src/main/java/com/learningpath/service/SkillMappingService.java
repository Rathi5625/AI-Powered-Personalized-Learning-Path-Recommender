package com.learningpath.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpath.dataset.RawCourseRecord;
import com.learningpath.entity.Course;
import com.learningpath.entity.CourseSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.SkillAlias;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CoverageLevel;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillDifficulty;
import com.learningpath.entity.enums.SkillMappingType;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.repository.CourseRepository;
import com.learningpath.repository.CourseSkillRepository;
import com.learningpath.repository.SkillAliasRepository;
import com.learningpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillMappingService {

    private final SkillRepository skillRepository;
    private final SkillAliasRepository skillAliasRepository;
    private final CourseRepository courseRepository;
    private final CourseSkillRepository courseSkillRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Transactional
    public void initializeAndLinkAll() {
        log.info("[SkillMappingService] Starting canonical skill synchronization and course-skill linking...");

        // 1. Ensure all 65 canonical skills exist in the skills table
        Map<String, Skill> canonicalSkillMap = ensureAllCanonicalSkillsExist();

        // 2. Populate the 61 persistent skill aliases (56 EXACT, 5 ALIAS)
        seedSkillAliases(canonicalSkillMap);

        // 3. Link all imported dataset courses to their resolved canonical Skill
        linkAllCuratedCourses();

        log.info("[SkillMappingService] Canonical skill synchronization and course linking completed successfully.");
    }

    @Transactional
    public Map<String, Skill> ensureAllCanonicalSkillsExist() {
        Map<String, SkillDefinition> definitions = getCanonicalSkillDefinitions();
        Map<String, Skill> resultMap = new HashMap<>();

        for (Map.Entry<String, SkillDefinition> entry : definitions.entrySet()) {
            String name = entry.getKey();
            SkillDefinition def = entry.getValue();

            Skill skill = skillRepository.findByName(name)
                    .orElseGet(() -> skillRepository.save(Skill.builder()
                            .name(name)
                            .category(def.category())
                            .description(def.description())
                            .difficulty(def.difficulty())
                            .build()));

            resultMap.put(name, skill);
        }

        log.info("[SkillMappingService] Ensured {} canonical skills in database.", resultMap.size());
        return resultMap;
    }

    @Transactional
    public void seedSkillAliases(Map<String, Skill> canonicalSkillMap) {
        List<AliasMappingDefinition> mappings = getStandardAliasMappings();

        int created = 0;
        int updated = 0;

        for (AliasMappingDefinition mapDef : mappings) {
            Skill targetSkill = canonicalSkillMap.get(mapDef.canonicalSkillName());
            if (targetSkill == null) {
                targetSkill = skillRepository.findByName(mapDef.canonicalSkillName()).orElse(null);
            }

            if (targetSkill == null) {
                log.warn("[SkillMappingService] Target canonical skill '{}' not found for dataset tag '{}'",
                        mapDef.canonicalSkillName(), mapDef.datasetSkillName());
                continue;
            }

            Optional<SkillAlias> existingOpt = skillAliasRepository.findByDatasetSkillNameIgnoreCase(mapDef.datasetSkillName());
            if (existingOpt.isEmpty()) {
                SkillAlias alias = SkillAlias.builder()
                        .datasetSkillName(mapDef.datasetSkillName())
                        .canonicalSkill(targetSkill)
                        .mappingType(mapDef.mappingType())
                        .confidence(mapDef.confidence())
                        .reason(mapDef.reason())
                        .build();
                skillAliasRepository.save(alias);
                created++;
            } else {
                SkillAlias existing = existingOpt.get();
                existing.setCanonicalSkill(targetSkill);
                existing.setMappingType(mapDef.mappingType());
                existing.setConfidence(mapDef.confidence());
                existing.setReason(mapDef.reason());
                skillAliasRepository.save(existing);
                updated++;
            }
        }

        log.info("[SkillMappingService] Seeded {} new and updated {} skill aliases.", created, updated);
    }

    @Transactional(readOnly = true)
    public Optional<Skill> resolveCanonicalSkill(String datasetSkillName) {
        if (datasetSkillName == null || datasetSkillName.isBlank()) {
            return Optional.empty();
        }

        // Check alias mapping first
        Optional<SkillAlias> aliasOpt = skillAliasRepository.findByDatasetSkillNameIgnoreCase(datasetSkillName.trim());
        if (aliasOpt.isPresent()) {
            return Optional.of(aliasOpt.get().getCanonicalSkill());
        }

        // Direct lookup by skill name
        return skillRepository.findByName(datasetSkillName.trim());
    }

    @Transactional
    public CourseLinkingSummary linkAllCuratedCourses() {
        List<RawCourseRecord> rawRecords = loadRawRecords();
        Map<String, String> courseSkillMap = new HashMap<>();
        for (RawCourseRecord r : rawRecords) {
            if (r.courseId() != null && r.skillTag() != null) {
                courseSkillMap.put(r.courseId().trim(), r.skillTag().trim());
            }
        }

        List<Course> allCourses = courseRepository.findAll();
        int linkedCount = 0;
        int alreadyLinkedCount = 0;
        int unresolvedCount = 0;
        List<String> unresolvedCourseCodes = new ArrayList<>();

        for (Course course : allCourses) {
            String courseCode = course.getCourseCode();
            if (courseCode == null || courseCode.isBlank()) {
                // Legacy baseline course, skip
                continue;
            }

            String skillTag = courseSkillMap.get(courseCode);
            if (skillTag == null) {
                continue;
            }

            Optional<Skill> canonicalSkillOpt = resolveCanonicalSkill(skillTag);
            if (canonicalSkillOpt.isEmpty()) {
                unresolvedCount++;
                unresolvedCourseCodes.add(courseCode + " (Tag: " + skillTag + ")");
                log.warn("[SkillMappingService] Could not resolve canonical skill for course {} with tag '{}'",
                        courseCode, skillTag);
                continue;
            }

            Skill canonicalSkill = canonicalSkillOpt.get();

            // Idempotent CourseSkill link check
            if (!courseSkillRepository.existsByCourseIdAndSkillId(course.getId(), canonicalSkill.getId())) {
                ProficiencyLevel targetProf = mapProficiencyFromDifficulty(course.getDifficulty());
                CourseSkill courseSkill = CourseSkill.builder()
                        .course(course)
                        .skill(canonicalSkill)
                        .coverageLevel(CoverageLevel.INTERMEDIATE)
                        .importance(SkillPriority.HIGH)
                        .targetProficiency(targetProf)
                        .isPrimarySkill(true)
                        .build();

                courseSkillRepository.save(courseSkill);
                linkedCount++;
            } else {
                alreadyLinkedCount++;
            }
        }

        log.info("[SkillMappingService] Course linking summary: Newly linked: {}, Already linked: {}, Unresolved: {}",
                linkedCount, alreadyLinkedCount, unresolvedCount);

        return new CourseLinkingSummary(linkedCount, alreadyLinkedCount, unresolvedCount, unresolvedCourseCodes);
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

    private List<RawCourseRecord> loadRawRecords() {
        try {
            Resource jsonResource = resourceLoader.getResource("classpath:data/techbot_courses.json");
            if (jsonResource.exists()) {
                try (InputStream is = jsonResource.getInputStream()) {
                    return objectMapper.readValue(is, new TypeReference<List<RawCourseRecord>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("[SkillMappingService] Failed to load classpath:data/techbot_courses.json: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private record SkillDefinition(String category, String description, SkillDifficulty difficulty) {}

    public record AliasMappingDefinition(String datasetSkillName, String canonicalSkillName, SkillMappingType mappingType, double confidence, String reason) {}

    public record CourseLinkingSummary(int newlyLinked, int alreadyLinked, int unresolved, List<String> unresolvedCodes) {}

    private Map<String, SkillDefinition> getCanonicalSkillDefinitions() {
        Map<String, SkillDefinition> defs = new LinkedHashMap<>();
        // Baseline 25 skills
        defs.put("Java", new SkillDefinition("Programming", "Core Java language, syntax, OOP & JVM internals", SkillDifficulty.INTERMEDIATE));
        defs.put("OOP", new SkillDefinition("Programming", "Object-Oriented Programming principles & design patterns", SkillDifficulty.BEGINNER));
        defs.put("Data Structures & Algorithms", new SkillDefinition("Computer Science", "Arrays, Trees, Graphs, Sorting & Searching algorithms", SkillDifficulty.INTERMEDIATE));
        defs.put("SQL", new SkillDefinition("Database", "Relational Database queries, indexing & transactions", SkillDifficulty.BEGINNER));
        defs.put("Spring Boot", new SkillDefinition("Backend Framework", "Spring Boot auto-config, dependency injection & REST services", SkillDifficulty.INTERMEDIATE));
        defs.put("REST APIs", new SkillDefinition("Web Services", "REST architectural style, HTTP methods & API design", SkillDifficulty.BEGINNER));
        defs.put("JPA/Hibernate", new SkillDefinition("Database", "Object-Relational Mapping, entity lifecycle & JPQL", SkillDifficulty.INTERMEDIATE));
        defs.put("Spring Security", new SkillDefinition("Security", "Authentication, authorization, OAuth2 & JWT tokens", SkillDifficulty.ADVANCED));
        defs.put("Testing", new SkillDefinition("DevOps & QA", "JUnit 5, Mockito, unit & integration testing", SkillDifficulty.BEGINNER));
        defs.put("Docker", new SkillDefinition("DevOps", "Containerization, Dockerfiles, compose & container networking", SkillDifficulty.INTERMEDIATE));
        defs.put("HTML", new SkillDefinition("Frontend", "HyperText Markup Language & semantic web markup", SkillDifficulty.BEGINNER));
        defs.put("CSS", new SkillDefinition("Frontend", "Cascading Style Sheets, Flexbox, Grid & responsive design", SkillDifficulty.BEGINNER));
        defs.put("JavaScript", new SkillDefinition("Frontend", "Modern ECMAScript, async/await, DOM manipulation", SkillDifficulty.BEGINNER));
        defs.put("TypeScript", new SkillDefinition("Frontend", "Static typing for JavaScript & advanced type definitions", SkillDifficulty.INTERMEDIATE));
        defs.put("React", new SkillDefinition("Frontend Framework", "React components, state management, hooks & virtual DOM", SkillDifficulty.INTERMEDIATE));
        defs.put("Git", new SkillDefinition("Tools", "Version control system, branching strategies & pull requests", SkillDifficulty.BEGINNER));
        defs.put("Python", new SkillDefinition("Programming", "Python language syntax, scripting & data structures", SkillDifficulty.BEGINNER));
        defs.put("Pandas", new SkillDefinition("Data Science", "Data manipulation & analysis library", SkillDifficulty.INTERMEDIATE));
        defs.put("NumPy", new SkillDefinition("Data Science", "Numerical computing & vector arrays library", SkillDifficulty.BEGINNER));
        defs.put("Statistics", new SkillDefinition("Mathematics", "Probability distributions, hypothesis testing & regression", SkillDifficulty.INTERMEDIATE));
        defs.put("Data Visualization", new SkillDefinition("Data Science", "Matplotlib, Seaborn & charting frameworks", SkillDifficulty.BEGINNER));
        defs.put("Machine Learning", new SkillDefinition("AI/ML", "Supervised/unsupervised algorithms, Scikit-learn", SkillDifficulty.INTERMEDIATE));
        defs.put("Deep Learning", new SkillDefinition("AI/ML", "Neural networks, CNNs, RNNs & Transformers", SkillDifficulty.ADVANCED));
        defs.put("TensorFlow/PyTorch", new SkillDefinition("AI/ML Framework", "Deep learning model design, training & evaluation", SkillDifficulty.ADVANCED));
        defs.put("MLOps", new SkillDefinition("DevOps & ML", "Machine learning pipeline deployment, monitoring & tracking", SkillDifficulty.ADVANCED));

        // Frontend Track Skills from DAG
        defs.put("Internet Basics", new SkillDefinition("Frontend", "HTTP/HTTPS protocols, DNS, hosting & browser mechanics", SkillDifficulty.BEGINNER));
        defs.put("CLI & Terminal Basics", new SkillDefinition("Tools", "Shell commands, file manipulation & terminal scripting", SkillDifficulty.BEGINNER));
        defs.put("Version Control(Git & GitHub)", new SkillDefinition("Tools", "Git repository management, branching & GitHub collaboration", SkillDifficulty.BEGINNER));
        defs.put("VCS Hosting", new SkillDefinition("Tools", "GitHub, GitLab & Bitbucket remote hosting workflows", SkillDifficulty.BEGINNER));
        defs.put("Package Managers", new SkillDefinition("Frontend", "npm, yarn, pnpm package lifecycle management", SkillDifficulty.BEGINNER));
        defs.put("CSS Frameworks", new SkillDefinition("Frontend", "Tailwind CSS, Bootstrap & utility-first styling", SkillDifficulty.INTERMEDIATE));
        defs.put("JavaScript Frameworks", new SkillDefinition("Frontend", "React, Vue, Angular component lifecycles", SkillDifficulty.INTERMEDIATE));
        defs.put("AI-Assisted Coding", new SkillDefinition("AI/Tooling", "Copilot, Cursor & generative AI developer tooling", SkillDifficulty.BEGINNER));
        defs.put("Generative AI for Frontend", new SkillDefinition("AI/Frontend", "Prompt engineering & LLM frontend interfaces", SkillDifficulty.INTERMEDIATE));
        defs.put("Implementing AI in Frontend", new SkillDefinition("AI/Frontend", "Client-side AI inference, WebGPU & embeddings", SkillDifficulty.ADVANCED));
        defs.put("Linters & formatters", new SkillDefinition("Tools", "ESLint, Prettier & code quality automation", SkillDifficulty.BEGINNER));
        defs.put("Module Bundlers", new SkillDefinition("Frontend", "Vite, Webpack, Rollup & build optimization", SkillDifficulty.INTERMEDIATE));
        defs.put("Auth Strategies", new SkillDefinition("Security", "JWT, OAuth2, session management & auth security", SkillDifficulty.ADVANCED));
        defs.put("Testing & Debugging", new SkillDefinition("QA", "Jest, Vitest, Cypress, Playwright & Chrome DevTools", SkillDifficulty.INTERMEDIATE));
        defs.put("Browser Web APIs", new SkillDefinition("Frontend", "DOM, Fetch, Storage, Canvas & Web Workers", SkillDifficulty.INTERMEDIATE));
        defs.put("Web Security", new SkillDefinition("Security", "CORS, XSS, CSRF, CSP & HTTPS defense", SkillDifficulty.ADVANCED));
        defs.put("Server-Side Rendering", new SkillDefinition("Frontend", "Next.js, Remix, SSR & streaming HTML", SkillDifficulty.ADVANCED));
        defs.put("Static Site Generators", new SkillDefinition("Frontend", "Astro, Next.js SSG & content driven web apps", SkillDifficulty.INTERMEDIATE));
        defs.put("Type Checkers", new SkillDefinition("Frontend", "TypeScript compiler, tsconfig & static analysis", SkillDifficulty.INTERMEDIATE));
        defs.put("Deployment", new SkillDefinition("DevOps", "Vercel, Netlify, Cloudflare Pages & hosting pipelines", SkillDifficulty.BEGINNER));
        defs.put("Design Systems", new SkillDefinition("Frontend", "Tokenization, accessibility, Storybook & atomic UI", SkillDifficulty.INTERMEDIATE));
        defs.put("Performance", new SkillDefinition("Frontend", "Core Web Vitals, lazy loading & memory profiling", SkillDifficulty.ADVANCED));
        defs.put("Web Components", new SkillDefinition("Frontend", "Custom Elements, Shadow DOM & HTML Templates", SkillDifficulty.INTERMEDIATE));
        defs.put("GraphQL", new SkillDefinition("Web Services", "Schemas, queries, mutations, subscriptions & Apollo", SkillDifficulty.INTERMEDIATE));
        defs.put("Accessibility", new SkillDefinition("Frontend", "WCAG standards, ARIA roles & screen reader testing", SkillDifficulty.BEGINNER));
        defs.put("Progressive Web Apps", new SkillDefinition("Frontend", "Service workers, offline caching & web app manifests", SkillDifficulty.ADVANCED));
        defs.put("Mobile Apps", new SkillDefinition("Mobile", "React Native & mobile web application development", SkillDifficulty.ADVANCED));
        defs.put("Desktop Applications in JavaScript", new SkillDefinition("Desktop", "Electron & desktop app architecture", SkillDifficulty.ADVANCED));

        // Backend Track Skills from DAG
        defs.put("Introduction to Backend Development", new SkillDefinition("Backend", "Client-server architecture, protocols & backend fundamentals", SkillDifficulty.BEGINNER));
        defs.put("Frontend Basics", new SkillDefinition("Frontend", "HTML, CSS, JS fundamentals for backend engineers", SkillDifficulty.BEGINNER));
        defs.put("Node.js Basics", new SkillDefinition("Backend", "Node event loop, buffers, streams & asynchronous runtime", SkillDifficulty.BEGINNER));
        defs.put("Express.js (Web Framework)", new SkillDefinition("Backend", "Middleware, routing & REST services with Express", SkillDifficulty.INTERMEDIATE));
        defs.put("REST APIs in Node", new SkillDefinition("Backend", "Building enterprise RESTful endpoints with Node.js", SkillDifficulty.INTERMEDIATE));
        defs.put("Testing (Node.js)", new SkillDefinition("QA", "Mocha, Chai, Jest & Supertest API testing", SkillDifficulty.INTERMEDIATE));
        defs.put("Django or Flask (Web Framework)", new SkillDefinition("Backend", "Python web backend development with Django & Flask", SkillDifficulty.INTERMEDIATE));
        defs.put("REST APIs in Python", new SkillDefinition("Backend", "FastAPI & Django REST framework development", SkillDifficulty.INTERMEDIATE));
        defs.put("Testing (Python)", new SkillDefinition("QA", "pytest, unittest & mock testing in Python", SkillDifficulty.INTERMEDIATE));
        defs.put("Databases (SQL)", new SkillDefinition("Database", "PostgreSQL, MySQL schema design & query optimization", SkillDifficulty.INTERMEDIATE));
        defs.put("NoSQL Databases", new SkillDefinition("Database", "MongoDB, Redis, document & key-value stores", SkillDifficulty.INTERMEDIATE));
        defs.put("Learn about Web Servers", new SkillDefinition("DevOps & Infra", "NGINX, Apache reverse proxies & load balancing", SkillDifficulty.INTERMEDIATE));
        defs.put("CI/CD Basics", new SkillDefinition("DevOps", "GitHub Actions, automated build pipelines & deployment", SkillDifficulty.INTERMEDIATE));
        defs.put("AI Assisted Coding", new SkillDefinition("AI/Tooling", "AI pair programming for backend development", SkillDifficulty.BEGINNER));
        defs.put("Learn the Basics (AI in Backend)", new SkillDefinition("AI/Backend", "Integrating AI model APIs in backend workflows", SkillDifficulty.INTERMEDIATE));
        defs.put("AI Applications in Software Development", new SkillDefinition("AI/Backend", "Agentic architectures, RAG & LLM backend patterns", SkillDifficulty.ADVANCED));
        defs.put("Integration Patterns (For AI)", new SkillDefinition("AI/Backend", "Vector databases, semantic caching & orchestration", SkillDifficulty.ADVANCED));
        defs.put("Caching", new SkillDefinition("Backend", "Redis, Memcached, distributed caching strategies", SkillDifficulty.INTERMEDIATE));
        defs.put("Search Engines", new SkillDefinition("Backend", "Elasticsearch, OpenSearch & full-text indexing", SkillDifficulty.ADVANCED));
        defs.put("Real Time Data", new SkillDefinition("Backend", "WebSockets, Server-Sent Events & Socket.io", SkillDifficulty.ADVANCED));
        defs.put("Message Brokers", new SkillDefinition("Backend", "RabbitMQ, Apache Kafka, event queues & pub/sub", SkillDifficulty.ADVANCED));
        defs.put("Scaling Databases", new SkillDefinition("Database", "Read replicas, sharding, partitioning & connection pooling", SkillDifficulty.ADVANCED));
        defs.put("Architectural Patterns", new SkillDefinition("Architecture", "Microservices, Clean Architecture & Event-Driven systems", SkillDifficulty.ADVANCED));
        defs.put("Building for Scale", new SkillDefinition("Architecture", "High availability, fault tolerance & distributed systems", SkillDifficulty.ADVANCED));

        return defs;
    }

    private List<AliasMappingDefinition> getStandardAliasMappings() {
        List<AliasMappingDefinition> list = new ArrayList<>();

        // 56 Exact Canonical Mappings
        String[] exactSkills = new String[]{
                "Internet Basics", "HTML", "CSS", "JavaScript", "CLI & Terminal Basics",
                "Version Control(Git & GitHub)", "VCS Hosting", "Package Managers", "CSS Frameworks",
                "JavaScript Frameworks", "AI-Assisted Coding", "Generative AI for Frontend",
                "Implementing AI in Frontend", "Linters & formatters", "Module Bundlers", "REST APIs",
                "Auth Strategies", "Testing & Debugging", "Browser Web APIs", "Web Security",
                "Server-Side Rendering", "Static Site Generators", "Type Checkers", "Deployment",
                "Design Systems", "Performance", "Web Components", "GraphQL", "Accessibility",
                "Progressive Web Apps", "Mobile Apps", "Desktop Applications in JavaScript",
                "Introduction to Backend Development", "Frontend Basics", "Node.js Basics",
                "Express.js (Web Framework)", "REST APIs in Node", "Testing (Node.js)",
                "Django or Flask (Web Framework)", "REST APIs in Python", "Testing (Python)",
                "Databases (SQL)", "NoSQL Databases", "Learn about Web Servers", "CI/CD Basics",
                "AI Assisted Coding", "Learn the Basics (AI in Backend)",
                "AI Applications in Software Development", "Integration Patterns (For AI)",
                "Caching", "Search Engines", "Real Time Data", "Message Brokers",
                "Scaling Databases", "Architectural Patterns", "Building for Scale"
        };

        for (String skillName : exactSkills) {
            list.add(new AliasMappingDefinition(skillName, skillName, SkillMappingType.EXACT, 1.00,
                    "Exact match with canonical skill '" + skillName + "' in curriculum DAG."));
        }

        // 5 Foundational Curriculum Aliases
        list.add(new AliasMappingDefinition("CSS Fundamentals", "CSS", SkillMappingType.ALIAS, 1.00,
                "Foundational curriculum variant of canonical skill 'CSS'."));
        list.add(new AliasMappingDefinition("HTML Fundamentals", "HTML", SkillMappingType.ALIAS, 1.00,
                "Foundational curriculum variant of canonical skill 'HTML'."));
        list.add(new AliasMappingDefinition("Internet Fundamentals", "Internet Basics", SkillMappingType.ALIAS, 1.00,
                "Curriculum variant for canonical skill 'Internet Basics'."));
        list.add(new AliasMappingDefinition("JavaScript Foundations", "JavaScript", SkillMappingType.ALIAS, 1.00,
                "Foundational curriculum variant of canonical skill 'JavaScript'."));
        list.add(new AliasMappingDefinition("Python Basics", "Python", SkillMappingType.ALIAS, 1.00,
                "Foundational curriculum variant of canonical skill 'Python'."));

        return list;
    }
}
