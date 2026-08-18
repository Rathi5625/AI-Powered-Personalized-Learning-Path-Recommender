package com.learningpath.skilldependency.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.learningpath.skilldependency.domain.SkillDependencyGraph;
import com.learningpath.skilldependency.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillDependencyService {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private final SkillDependencyGraph graph = new SkillDependencyGraph();
    private int domainCount = 0;
    private int skillCount = 0;

    @PostConstruct
    public void initialize() {
        log.info("[SkillDependencyService] Loading canonical skill prerequisites dataset...");
        try {
            Resource resource = resourceLoader.getResource("classpath:data/skill_prerequisites.json");
            if (!resource.exists()) {
                log.error("[SkillDependencyService] Canonical dataset not found at classpath:data/skill_prerequisites.json");
                throw new IllegalStateException("skill_prerequisites.json dataset is missing");
            }

            try (InputStream is = resource.getInputStream()) {
                Map<String, Map<String, List<String>>> data = objectMapper.readValue(
                        is, new TypeReference<Map<String, Map<String, List<String>>>>() {}
                );

                this.domainCount = data.size();
                Set<String> uniqueSkills = new HashSet<>();

                for (Map.Entry<String, Map<String, List<String>>> domainEntry : data.entrySet()) {
                    String domainName = domainEntry.getKey();
                    Map<String, List<String>> skillsMap = domainEntry.getValue();

                    if (skillsMap == null) continue;

                    for (Map.Entry<String, List<String>> skillEntry : skillsMap.entrySet()) {
                        String skillName = skillEntry.getKey();
                        if (skillName == null || skillName.trim().startsWith("---")) {
                            continue;
                        }
                        List<String> prereqs = skillEntry.getValue();

                        graph.addSkill(skillName, prereqs);
                        uniqueSkills.add(skillName);
                        if (prereqs != null) {
                            for (String p : prereqs) {
                                if (p != null && !p.trim().startsWith("---")) {
                                    uniqueSkills.add(p);
                                }
                            }
                        }
                    }
                }

                // Register canonical skill aliases
                registerStandardAliases();

                this.skillCount = uniqueSkills.size();
                log.info("[SkillDependencyService] Loaded {} domains and {} unique skills into graph.", domainCount, skillCount);
            }

            // Validate cycle freedom
            if (graph.hasCircularDependency()) {
                log.error("[SkillDependencyService] Invalid dependency graph: Circular dependency detected in dataset!");
                throw new IllegalStateException("Circular dependency detected in skill_prerequisites.json");
            }

            log.info("[SkillDependencyService] Skill Dependency Engine initialized successfully.");

        } catch (Exception e) {
            log.error("[SkillDependencyService] Failed to initialize Skill Dependency Engine: {}", e.getMessage());
            throw new RuntimeException("Skill Dependency Engine initialization failed", e);
        }
    }

    private void registerStandardAliases() {
        graph.registerAlias("Internet Fundamentals", "Internet Basics");
        graph.registerAlias("Databases (SQL)", "SQL Databases");
        graph.registerAlias("SQL", "SQL Databases");
        graph.registerAlias("Python Basics", "Python");
        graph.registerAlias("Testing & Debugging", "Testing");
        graph.registerAlias("Testing", "Testing & Debugging");
        graph.registerAlias("Version Control(Git & GitHub)", "Git");
        graph.registerAlias("Git", "Version Control(Git & GitHub)");
        graph.registerAlias("HTML Fundamentals", "HTML");
        graph.registerAlias("CSS Fundamentals", "CSS");
        graph.registerAlias("JavaScript Foundations", "JavaScript");
        graph.registerAlias("REST APIs in Node", "Backend REST APIs");
        graph.registerAlias("AI-Assisted Coding", "AI Assisted Coding");
        graph.registerAlias("AI Assisted Coding", "AI-Assisted Coding");
        graph.registerAlias("Express.js", "Express.js (Web Framework)");
        graph.registerAlias("Django or Flask", "Django or Flask (Web Framework)");
        graph.registerAlias("React", "JavaScript Frameworks");
        graph.registerAlias("JavaScript Frameworks", "React");
    }

    public int getDomainCount() {
        return domainCount;
    }

    public int getSkillCount() {
        return skillCount;
    }

    public boolean hasCircularDependency() {
        return graph.hasCircularDependency();
    }

    public PrerequisitesResponse getPrerequisites(String skillName) {
        if (!graph.containsSkill(skillName)) {
            return PrerequisitesResponse.unknown(skillName);
        }

        String canonical = graph.getCanonicalName(skillName);
        List<String> direct = graph.getDirectPrerequisites(canonical);
        List<String> recursive = graph.getTransitivePrerequisites(canonical);

        return new PrerequisitesResponse(canonical, true, direct, recursive);
    }

    public DependentsResponse getDependents(String skillName) {
        if (!graph.containsSkill(skillName)) {
            return DependentsResponse.unknown(skillName);
        }

        String canonical = graph.getCanonicalName(skillName);
        List<String> direct = graph.getDirectDependents(canonical);
        List<String> recursive = graph.getTransitiveDependents(canonical);

        return new DependentsResponse(canonical, true, direct, recursive);
    }

    public LearningOrderResponse getLearningOrder(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return LearningOrderResponse.ok(Collections.emptyList(), Collections.emptyList());
        }

        Set<String> validTargets = new LinkedHashSet<>();
        List<String> unknownSkills = new ArrayList<>();

        for (String skill : skills) {
            if (skill == null || skill.trim().isEmpty()) continue;
            if (graph.containsSkill(skill)) {
                validTargets.add(graph.getCanonicalName(skill));
            } else {
                unknownSkills.add(skill.trim());
            }
        }

        if (validTargets.isEmpty()) {
            return LearningOrderResponse.ok(Collections.emptyList(), unknownSkills);
        }

        try {
            List<String> orderedSkills = graph.getTopologicalOrderForSkills(validTargets);
            return LearningOrderResponse.ok(orderedSkills, unknownSkills);
        } catch (Exception e) {
            log.error("[SkillDependencyService] Topological sort error: {}", e.getMessage());
            return LearningOrderResponse.fail("Circular dependency detected or invalid graph traversal");
        }
    }

    public MissingPrerequisitesResponse getMissingPrerequisites(List<String> currentSkills, List<String> targetSkills) {
        if (targetSkills == null || targetSkills.isEmpty()) {
            return MissingPrerequisitesResponse.ok(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        Set<String> normalizedCurrent = new HashSet<>();
        if (currentSkills != null) {
            for (String s : currentSkills) {
                if (s != null && graph.containsSkill(s)) {
                    normalizedCurrent.add(graph.getCanonicalName(s));
                }
            }
        }

        Set<String> validTargets = new LinkedHashSet<>();
        List<String> unknownSkills = new ArrayList<>();

        for (String target : targetSkills) {
            if (target == null || target.trim().isEmpty()) continue;
            if (graph.containsSkill(target)) {
                validTargets.add(graph.getCanonicalName(target));
            } else {
                unknownSkills.add(target.trim());
            }
        }

        if (validTargets.isEmpty()) {
            return MissingPrerequisitesResponse.ok(Collections.emptyList(), new ArrayList<>(normalizedCurrent), unknownSkills);
        }

        // Get full topological learning order for all required target skills and prerequisites
        List<String> fullOrder = graph.getTopologicalOrderForSkills(validTargets);

        List<String> missingPrereqs = new ArrayList<>();
        List<String> satisfiedPrereqs = new ArrayList<>();

        for (String skill : fullOrder) {
            if (normalizedCurrent.contains(skill)) {
                satisfiedPrereqs.add(skill);
            } else {
                missingPrereqs.add(skill);
            }
        }

        return MissingPrerequisitesResponse.ok(missingPrereqs, satisfiedPrereqs, unknownSkills);
    }
}
