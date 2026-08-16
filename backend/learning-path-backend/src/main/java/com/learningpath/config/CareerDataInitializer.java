package com.learningpath.config;

import com.learningpath.entity.Career;
import com.learningpath.entity.CareerSkill;
import com.learningpath.entity.Skill;
import com.learningpath.entity.enums.ProficiencyLevel;
import com.learningpath.entity.enums.SkillDifficulty;
import com.learningpath.entity.enums.SkillPriority;
import com.learningpath.repository.CareerRepository;
import com.learningpath.repository.CareerSkillRepository;
import com.learningpath.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CareerDataInitializer implements CommandLineRunner {

    private final CareerRepository careerRepository;
    private final SkillRepository skillRepository;
    private final CareerSkillRepository careerSkillRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (careerRepository.count() > 0) {
            log.info("Careers already initialized. Skipping seed data insertion.");
            return;
        }

        log.info("Initializing realistic seed data for Careers and Career Skills...");

        // 1. Seed Skills Catalog
        Map<String, Skill> skills = seedSkills();

        // 2. Seed Careers & Requirements
        seedJavaBackendDeveloper(skills);
        seedFrontendDeveloper(skills);
        seedFullStackDeveloper(skills);
        seedDataScientist(skills);
        seedMachineLearningEngineer(skills);

        log.info("Career and Skill seed data successfully populated.");
    }

    private Map<String, Skill> seedSkills() {
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

    private void seedJavaBackendDeveloper(Map<String, Skill> skills) {
        Career career = careerRepository.save(Career.builder()
                .title("Java Backend Developer")
                .description("Build scalable, robust server-side APIs and microservices using Java and Spring Boot.")
                .category("Software Engineering")
                .industry("Information Technology")
                .build());

        addSkill(career, skills.get("Java"), SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("OOP"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Data Structures & Algorithms"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("SQL"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Spring Boot"), SkillPriority.CRITICAL, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("REST APIs"), SkillPriority.HIGH, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("JPA/Hibernate"), SkillPriority.HIGH, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("Spring Security"), SkillPriority.MEDIUM, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("Testing"), SkillPriority.MEDIUM, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("Docker"), SkillPriority.MEDIUM, ProficiencyLevel.BEGINNER);
    }

    private void seedFrontendDeveloper(Map<String, Skill> skills) {
        Career career = careerRepository.save(Career.builder()
                .title("Frontend Developer")
                .description("Craft responsive, interactive, and high-performance user interfaces for modern web applications.")
                .category("Software Engineering")
                .industry("Information Technology")
                .build());

        addSkill(career, skills.get("HTML"), SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED);
        addSkill(career, skills.get("CSS"), SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED);
        addSkill(career, skills.get("JavaScript"), SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED);
        addSkill(career, skills.get("TypeScript"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("React"), SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Git"), SkillPriority.HIGH, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("REST APIs"), SkillPriority.HIGH, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("Testing"), SkillPriority.MEDIUM, ProficiencyLevel.BEGINNER);
    }

    private void seedFullStackDeveloper(Map<String, Skill> skills) {
        Career career = careerRepository.save(Career.builder()
                .title("Full Stack Developer")
                .description("Develop end-to-end applications spanning user-facing frontends and server-side backend services.")
                .category("Software Engineering")
                .industry("Information Technology")
                .build());

        addSkill(career, skills.get("Java"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Spring Boot"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("JavaScript"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("React"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("SQL"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("REST APIs"), SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Git"), SkillPriority.HIGH, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("Docker"), SkillPriority.MEDIUM, ProficiencyLevel.BEGINNER);
    }

    private void seedDataScientist(Map<String, Skill> skills) {
        Career career = careerRepository.save(Career.builder()
                .title("Data Scientist")
                .description("Analyze complex datasets, uncover business insights, and build predictive statistical models.")
                .category("Data & Analytics")
                .industry("Data Science")
                .build());

        addSkill(career, skills.get("Python"), SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED);
        addSkill(career, skills.get("SQL"), SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Statistics"), SkillPriority.CRITICAL, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Pandas"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("NumPy"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Data Visualization"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Machine Learning"), SkillPriority.HIGH, ProficiencyLevel.BEGINNER);
    }

    private void seedMachineLearningEngineer(Map<String, Skill> skills) {
        Career career = careerRepository.save(Career.builder()
                .title("Machine Learning Engineer")
                .description("Design, train, optimize, and deploy intelligent machine learning and deep learning models into production.")
                .category("Artificial Intelligence")
                .industry("AI & Machine Learning")
                .build());

        addSkill(career, skills.get("Python"), SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED);
        addSkill(career, skills.get("Machine Learning"), SkillPriority.CRITICAL, ProficiencyLevel.ADVANCED);
        addSkill(career, skills.get("Deep Learning"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("TensorFlow/PyTorch"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("Data Structures & Algorithms"), SkillPriority.HIGH, ProficiencyLevel.INTERMEDIATE);
        addSkill(career, skills.get("MLOps"), SkillPriority.HIGH, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("Docker"), SkillPriority.MEDIUM, ProficiencyLevel.BEGINNER);
        addSkill(career, skills.get("SQL"), SkillPriority.MEDIUM, ProficiencyLevel.BEGINNER);
    }

    private void addSkill(Career career, Skill skill, SkillPriority priority, ProficiencyLevel requiredProficiency) {
        if (skill == null) return;
        careerSkillRepository.save(CareerSkill.builder()
                .career(career)
                .skill(skill)
                .priority(priority)
                .requiredProficiency(requiredProficiency)
                .isMandatory(priority == SkillPriority.CRITICAL || priority == SkillPriority.HIGH)
                .build());
    }

    private record SkillDefinition(String category, String description, SkillDifficulty difficulty) {}
}
