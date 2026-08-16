package com.learningpath.skilldependency.service;

import tools.jackson.databind.ObjectMapper;
import com.learningpath.skilldependency.domain.SkillDependencyGraph;
import com.learningpath.skilldependency.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SkillDependencyServiceTest {

    private SkillDependencyService service;

    @BeforeEach
    void setUp() {
        service = new SkillDependencyService(new DefaultResourceLoader(), new ObjectMapper());
        service.initialize();
    }

    @Test
    void testInitialization_LoadsDatasetSuccessfully() {
        assertTrue(service.getDomainCount() > 0);
        assertTrue(service.getSkillCount() > 0);
        assertFalse(service.hasCircularDependency());
    }

    @Test
    void testGetPrerequisites_DirectAndRecursive() {
        PrerequisitesResponse response = service.getPrerequisites("JavaScript");

        assertTrue(response.found());
        assertEquals("JavaScript", response.skill());
        assertTrue(response.directPrerequisites().containsAll(List.of("HTML", "CSS")));
        assertTrue(response.recursivePrerequisites().containsAll(List.of("Internet Basics", "HTML", "CSS")));
    }

    @Test
    void testGetDependents_DirectAndRecursive() {
        DependentsResponse response = service.getDependents("HTML");

        assertTrue(response.found());
        assertEquals("HTML", response.skill());
        assertTrue(response.directDependents().containsAll(List.of("CSS", "JavaScript")));
        assertTrue(response.recursiveDependents().containsAll(List.of("CSS", "JavaScript", "JavaScript Frameworks", "REST APIs", "GraphQL")));
    }

    @Test
    void testGetLearningOrder_TopologicalSort() {
        LearningOrderResponse response = service.getLearningOrder(List.of("CSS", "JavaScript", "HTML"));

        assertTrue(response.success());
        List<String> order = response.learningOrder();
        assertNotNull(order);

        // Topological ordering rules:
        // Internet Basics before HTML
        // HTML before CSS & JavaScript
        // CSS before JavaScript
        assertTrue(order.indexOf("Internet Basics") < order.indexOf("HTML"));
        assertTrue(order.indexOf("HTML") < order.indexOf("CSS"));
        assertTrue(order.indexOf("CSS") < order.indexOf("JavaScript"));
    }

    @Test
    void testGetMissingPrerequisites_CalculatesSkillGapsCorrectly() {
        List<String> current = List.of("Internet Basics", "HTML");
        List<String> target = List.of("JavaScript", "JavaScript Frameworks");

        MissingPrerequisitesResponse response = service.getMissingPrerequisites(current, target);

        assertTrue(response.success());
        assertTrue(response.missingPrerequisites().containsAll(List.of("CSS", "JavaScript", "Package Managers", "JavaScript Frameworks")));
        assertFalse(response.missingPrerequisites().contains("Internet Basics"));
        assertFalse(response.missingPrerequisites().contains("HTML"));
    }

    @Test
    void testGetPrerequisites_UnknownSkill_ReturnsFoundFalse() {
        PrerequisitesResponse response = service.getPrerequisites("Quantum Computing");

        assertFalse(response.found());
        assertEquals("Quantum Computing", response.skill());
        assertTrue(response.directPrerequisites().isEmpty());
        assertTrue(response.recursivePrerequisites().isEmpty());
    }

    @Test
    void testCircularDependencyDetection_DetectsCyclesInCustomGraph() {
        SkillDependencyGraph customGraph = new SkillDependencyGraph();
        customGraph.addSkill("A", List.of("B"));
        customGraph.addSkill("B", List.of("C"));
        customGraph.addSkill("C", List.of("A"));

        assertTrue(customGraph.hasCircularDependency());
    }
}
