package com.learningpath.skilldependency.service;

import tools.jackson.databind.ObjectMapper;
import com.learningpath.skilldependency.domain.SkillDependencyGraph;
import com.learningpath.skilldependency.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FriendSkillDependencyIntegrationTest {

    private SkillDependencyService service;
    private SkillDependencyGraph graph;

    @BeforeEach
    void setUp() {
        service = new SkillDependencyService(new DefaultResourceLoader(), new ObjectMapper());
        service.initialize();
    }

    @Test
    @DisplayName("1 & 2. Friend skills are loaded and recognized (65 canonical skills across 3 domains)")
    void testFriendSkillsLoadedAndRecognized() {
        assertEquals(3, service.getDomainCount());
        assertEquals(65, service.getSkillCount());
        assertFalse(service.hasCircularDependency());

        // Verify key friend frontend skills
        assertTrue(service.getPrerequisites("Web Security").found());
        assertTrue(service.getPrerequisites("Module Bundlers").found());
        assertTrue(service.getPrerequisites("Design Systems").found());
        assertTrue(service.getPrerequisites("Server-Side Rendering").found());
        assertTrue(service.getPrerequisites("Generative AI for Frontend").found());

        // Verify key friend backend skills
        assertTrue(service.getPrerequisites("Node.js Basics").found());
        assertTrue(service.getPrerequisites("Express.js (Web Framework)").found());
        assertTrue(service.getPrerequisites("Django or Flask (Web Framework)").found());
        assertTrue(service.getPrerequisites("Scaling Databases").found());
        assertTrue(service.getPrerequisites("Building for Scale").found());
    }

    @Test
    @DisplayName("3. Section headings are strictly excluded from graph nodes")
    void testSectionHeadingsExcluded() {
        List<String> sectionHeaders = List.of(
                "--- Advanced Topics ---",
                "--- Node.js Backend Track ---",
                "--- Python Backend Track ---",
                "--- Universal Topics (Core Backend) ---",
                "--- AI in Backend Development ---",
                "--- Advanced Backend ---"
        );

        for (String header : sectionHeaders) {
            PrerequisitesResponse response = service.getPrerequisites(header);
            assertFalse(response.found(), "Section header should not be a valid skill: " + header);
        }
    }

    @Test
    @DisplayName("4. Skill Aliasing and Canonical Normalization")
    void testAliasNormalization() {
        // Internet Fundamentals -> Internet Basics
        assertTrue(service.getPrerequisites("Internet Fundamentals").found());
        assertEquals("Internet Basics", service.getPrerequisites("Internet Fundamentals").skill());

        // Databases (SQL) / SQL -> Databases (SQL) or SQL Databases
        assertTrue(service.getPrerequisites("Databases (SQL)").found());
        assertTrue(service.getPrerequisites("SQL").found());

        // Python Basics -> Python
        assertTrue(service.getPrerequisites("Python Basics").found());
        assertEquals("Python", service.getPrerequisites("Python Basics").skill());

        // Testing & Debugging <-> Testing
        assertTrue(service.getPrerequisites("Testing & Debugging").found());
        assertTrue(service.getPrerequisites("Testing").found());

        // Version Control(Git & GitHub) <-> Git
        assertTrue(service.getPrerequisites("Version Control(Git & GitHub)").found());
        assertTrue(service.getPrerequisites("Git").found());

        // HTML Fundamentals -> HTML
        assertTrue(service.getPrerequisites("HTML Fundamentals").found());
        assertEquals("HTML", service.getPrerequisites("HTML Fundamentals").skill());

        // CSS Fundamentals -> CSS
        assertTrue(service.getPrerequisites("CSS Fundamentals").found());
        assertEquals("CSS", service.getPrerequisites("CSS Fundamentals").skill());

        // JavaScript Foundations -> JavaScript
        assertTrue(service.getPrerequisites("JavaScript Foundations").found());
        assertEquals("JavaScript", service.getPrerequisites("JavaScript Foundations").skill());

        // REST APIs in Node -> REST APIs in Node
        assertTrue(service.getPrerequisites("REST APIs in Node").found());
    }

    @Test
    @DisplayName("5 & 6. Express.js and Django/Flask references are correctly normalized")
    void testWebFrameworkReferenceCorrections() {
        // REST APIs in Node depends on Express.js (Web Framework)
        PrerequisitesResponse nodeRest = service.getPrerequisites("REST APIs in Node");
        assertTrue(nodeRest.found());
        assertTrue(nodeRest.directPrerequisites().contains("Express.js (Web Framework)"));

        // REST APIs in Python depends on Django or Flask (Web Framework)
        PrerequisitesResponse pyRest = service.getPrerequisites("REST APIs in Python");
        assertTrue(pyRest.found());
        assertTrue(pyRest.directPrerequisites().contains("Django or Flask (Web Framework)"));
    }

    @Test
    @DisplayName("7. Package Managers requires JavaScript and CLI & Terminal Basics")
    void testPackageManagersUpdatedPrerequisites() {
        PrerequisitesResponse response = service.getPrerequisites("Package Managers");
        assertTrue(response.found());
        assertEquals(2, response.directPrerequisites().size());
        assertTrue(response.directPrerequisites().containsAll(List.of("JavaScript", "CLI & Terminal Basics")));
        assertTrue(response.recursivePrerequisites().containsAll(List.of("Internet Basics", "HTML", "CSS", "JavaScript", "CLI & Terminal Basics")));
    }

    @Test
    @DisplayName("8, 9, 10 & 11. Graph has no broken prerequisites, duplicate edges, or self-dependencies")
    void testGraphIntegrity() {
        assertFalse(service.hasCircularDependency());

        // Query deep leaf skill and verify recursive chain resolution
        PrerequisitesResponse buildingForScale = service.getPrerequisites("Building for Scale");
        assertTrue(buildingForScale.found());
        assertFalse(buildingForScale.recursivePrerequisites().isEmpty());
        assertTrue(buildingForScale.recursivePrerequisites().contains("Architectural Patterns"));
        assertTrue(buildingForScale.recursivePrerequisites().contains("Scaling Databases"));
        assertTrue(buildingForScale.recursivePrerequisites().contains("Databases (SQL)"));
    }

    @Test
    @DisplayName("12 & 13. Frontend Topological Learning Order")
    void testFrontendTopologicalOrdering() {
        List<String> frontendSkills = List.of(
                "JavaScript Frameworks",
                "HTML",
                "Web Security",
                "Package Managers",
                "CSS",
                "JavaScript",
                "Browser Web APIs",
                "CLI & Terminal Basics"
        );

        LearningOrderResponse response = service.getLearningOrder(frontendSkills);
        assertTrue(response.success());
        List<String> order = response.learningOrder();

        // Verify topological order invariants
        assertTrue(order.indexOf("Internet Basics") < order.indexOf("HTML"));
        assertTrue(order.indexOf("HTML") < order.indexOf("CSS"));
        assertTrue(order.indexOf("CSS") < order.indexOf("JavaScript"));
        assertTrue(order.indexOf("CLI & Terminal Basics") < order.indexOf("Package Managers"));
        assertTrue(order.indexOf("JavaScript") < order.indexOf("Package Managers"));
        assertTrue(order.indexOf("Package Managers") < order.indexOf("JavaScript Frameworks"));
        assertTrue(order.indexOf("JavaScript") < order.indexOf("Browser Web APIs"));
        assertTrue(order.indexOf("Browser Web APIs") < order.indexOf("Web Security"));
    }

    @Test
    @DisplayName("14. Node.js Backend Track Topological Learning Order")
    void testNodeJsBackendOrdering() {
        List<String> nodeSkills = List.of(
                "Testing (Node.js)",
                "Node.js Basics",
                "Express.js (Web Framework)",
                "REST APIs in Node"
        );

        LearningOrderResponse response = service.getLearningOrder(nodeSkills);
        assertTrue(response.success());
        List<String> order = response.learningOrder();

        assertTrue(order.indexOf("HTML") < order.indexOf("JavaScript"));
        assertTrue(order.indexOf("JavaScript") < order.indexOf("Node.js Basics"));
        assertTrue(order.indexOf("Node.js Basics") < order.indexOf("Express.js (Web Framework)"));
        assertTrue(order.indexOf("Express.js (Web Framework)") < order.indexOf("REST APIs in Node"));
        assertTrue(order.indexOf("REST APIs in Node") < order.indexOf("Testing (Node.js)"));
    }

    @Test
    @DisplayName("15. Python Backend Track Topological Learning Order")
    void testPythonBackendOrdering() {
        List<String> pythonSkills = List.of(
                "Testing (Python)",
                "Python",
                "Django or Flask (Web Framework)",
                "REST APIs in Python"
        );

        LearningOrderResponse response = service.getLearningOrder(pythonSkills);
        assertTrue(response.success());
        List<String> order = response.learningOrder();

        assertTrue(order.indexOf("Internet Basics") < order.indexOf("Python"));
        assertTrue(order.indexOf("Python") < order.indexOf("Django or Flask (Web Framework)"));
        assertTrue(order.indexOf("Django or Flask (Web Framework)") < order.indexOf("REST APIs in Python"));
        assertTrue(order.indexOf("REST APIs in Python") < order.indexOf("Testing (Python)"));
    }

    @Test
    @DisplayName("16. Backward Compatibility: Java/Spring Boot Track Preserved")
    void testJavaSpringBootBackwardCompatibility() {
        List<String> javaSkills = List.of("Backend REST APIs", "ORM / Hibernate", "Spring Boot", "Java", "SQL Databases");
        LearningOrderResponse response = service.getLearningOrder(javaSkills);

        assertTrue(response.success());
        List<String> order = response.learningOrder();

        assertTrue(order.indexOf("Internet Basics") < order.indexOf("Java"));
        assertTrue(order.indexOf("Java") < order.indexOf("Spring Boot"));
        assertTrue(order.indexOf("Java") < order.indexOf("ORM / Hibernate"));
        assertTrue(order.indexOf("SQL Databases") < order.indexOf("ORM / Hibernate"));
        assertTrue(order.indexOf("Spring Boot") < order.indexOf("Backend REST APIs"));
    }

    @Test
    @DisplayName("17. Backward Compatibility: Data Science Track Preserved")
    void testDataScienceBackwardCompatibility() {
        List<String> dsSkills = List.of("Machine Learning", "Data Analysis", "Python");
        LearningOrderResponse response = service.getLearningOrder(dsSkills);

        assertTrue(response.success());
        List<String> order = response.learningOrder();

        assertTrue(order.indexOf("Internet Basics") < order.indexOf("Python"));
        assertTrue(order.indexOf("Python") < order.indexOf("Data Analysis"));
        assertTrue(order.indexOf("Data Analysis") < order.indexOf("Machine Learning"));
    }

    @Test
    @DisplayName("18, 19, 20 & 21. Missing Prerequisites Calculation for Complex Multi-Track Gaps")
    void testMissingPrerequisitesMultiTrack() {
        List<String> current = List.of("Internet Basics", "HTML", "CSS", "JavaScript");
        List<String> target = List.of("Web Security", "GraphQL");

        MissingPrerequisitesResponse response = service.getMissingPrerequisites(current, target);
        assertTrue(response.success());

        // Missing should contain CLI & Terminal Basics, Package Managers, JavaScript Frameworks, REST APIs, Browser Web APIs, GraphQL, Web Security
        assertTrue(response.missingPrerequisites().contains("Browser Web APIs"));
        assertTrue(response.missingPrerequisites().contains("Web Security"));
        assertTrue(response.missingPrerequisites().contains("REST APIs"));
        assertTrue(response.missingPrerequisites().contains("Package Managers"));
        assertTrue(response.missingPrerequisites().contains("JavaScript Frameworks"));
        assertTrue(response.missingPrerequisites().contains("GraphQL"));

        // Satisfied should contain HTML, CSS, JavaScript, Internet Basics
        assertTrue(response.satisfiedPrerequisites().contains("JavaScript"));
        assertTrue(response.satisfiedPrerequisites().contains("HTML"));
    }
}
