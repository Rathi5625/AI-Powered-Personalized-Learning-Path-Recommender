package com.learningpath.skilldependency.domain;

import java.util.*;

/**
 * In-memory Graph data structure representing skill prerequisite and dependent relationships.
 */
public class SkillDependencyGraph {

    // Canonical skill name mapping (lowercase -> original case)
    private final Map<String, String> canonicalNameMap = new HashMap<>();

    // Direct Prerequisites: Skill -> Set of Prerequisites
    private final Map<String, Set<String>> prerequisites = new HashMap<>();

    // Direct Dependents: Skill -> Set of Dependents
    private final Map<String, Set<String>> dependents = new HashMap<>();

    // Set of all known skill names (original casing)
    private final Set<String> allSkills = new LinkedHashSet<>();

    /**
     * Registers a skill and its direct prerequisites.
     */
    public void addSkill(String skillName, List<String> directPrereqs) {
        if (skillName == null || skillName.trim().isEmpty()) {
            return;
        }

        String canonicalSkill = skillName.trim();
        String lowerSkill = canonicalSkill.toLowerCase();

        canonicalNameMap.put(lowerSkill, canonicalSkill);
        allSkills.add(canonicalSkill);
        prerequisites.putIfAbsent(canonicalSkill, new LinkedHashSet<>());
        dependents.putIfAbsent(canonicalSkill, new LinkedHashSet<>());

        if (directPrereqs != null) {
            for (String prereq : directPrereqs) {
                if (prereq == null || prereq.trim().isEmpty()) {
                    continue;
                }
                String canonicalPrereq = prereq.trim();
                String lowerPrereq = canonicalPrereq.toLowerCase();

                canonicalNameMap.putIfAbsent(lowerPrereq, canonicalPrereq);
                allSkills.add(canonicalPrereq);

                // Add to prerequisites of skillName
                prerequisites.get(canonicalSkill).add(canonicalPrereq);

                // Add to dependents of prereq
                dependents.putIfAbsent(canonicalPrereq, new LinkedHashSet<>());
                dependents.get(canonicalPrereq).add(canonicalSkill);

                // Ensure prereq entry exists in prerequisites map
                prerequisites.putIfAbsent(canonicalPrereq, new LinkedHashSet<>());
            }
        }
    }

    public boolean containsSkill(String skillName) {
        return skillName != null && canonicalNameMap.containsKey(skillName.trim().toLowerCase());
    }

    public String getCanonicalName(String skillName) {
        if (skillName == null) return null;
        return canonicalNameMap.get(skillName.trim().toLowerCase());
    }

    public Set<String> getAllSkills() {
        return Collections.unmodifiableSet(allSkills);
    }

    public List<String> getDirectPrerequisites(String skillName) {
        String canonical = getCanonicalName(skillName);
        if (canonical == null || !prerequisites.containsKey(canonical)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(prerequisites.get(canonical));
    }

    public List<String> getTransitivePrerequisites(String skillName) {
        String canonical = getCanonicalName(skillName);
        if (canonical == null) {
            return Collections.emptyList();
        }

        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(canonical);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> direct = prerequisites.getOrDefault(current, Collections.emptySet());
            for (String p : direct) {
                if (!p.equals(canonical) && visited.add(p)) {
                    queue.add(p);
                }
            }
        }
        return new ArrayList<>(visited);
    }

    public List<String> getDirectDependents(String skillName) {
        String canonical = getCanonicalName(skillName);
        if (canonical == null || !dependents.containsKey(canonical)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(dependents.get(canonical));
    }

    public List<String> getTransitiveDependents(String skillName) {
        String canonical = getCanonicalName(skillName);
        if (canonical == null) {
            return Collections.emptyList();
        }

        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(canonical);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> direct = dependents.getOrDefault(current, Collections.emptySet());
            for (String d : direct) {
                if (!d.equals(canonical) && visited.add(d)); {
                    queue.add(d);
                }
            }
        }
        return new ArrayList<>(visited);
    }

    /**
     * Detects circular dependencies in the graph using DFS recursion stack.
     */
    public boolean hasCircularDependency() {
        Map<String, Integer> state = new HashMap<>(); // 0: unvisited, 1: visiting, 2: visited

        for (String skill : allSkills) {
            if (state.getOrDefault(skill, 0) == 0) {
                if (hasCycleDfs(skill, state)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasCycleDfs(String node, Map<String, Integer> state) {
        state.put(node, 1); // visiting

        Set<String> prereqs = prerequisites.getOrDefault(node, Collections.emptySet());
        for (String p : prereqs) {
            int pState = state.getOrDefault(p, 0);
            if (pState == 1) {
                return true; // cycle detected
            }
            if (pState == 0) {
                if (hasCycleDfs(p, state)) {
                    return true;
                }
            }
        }

        state.put(node, 2); // visited
        return false;
    }

    /**
     * Returns topological learning order for a target subset of skills (including all required prerequisites).
     */
    public List<String> getTopologicalOrderForSkills(Set<String> targetSkills) {
        // Collect target skills and all their transitive prerequisites
        Set<String> subGraph = new LinkedHashSet<>();
        for (String skill : targetSkills) {
            String canonical = getCanonicalName(skill);
            if (canonical != null) {
                subGraph.add(canonical);
                subGraph.addAll(getTransitivePrerequisites(canonical));
            }
        }

        if (subGraph.isEmpty()) {
            return Collections.emptyList();
        }

        // Calculate in-degree within the subGraph
        // Note: For learning order, A -> B means A must be learned BEFORE B.
        // So in-degree of B is number of prerequisites of B inside subGraph.
        Map<String, Integer> inDegree = new HashMap<>();
        for (String node : subGraph) {
            inDegree.put(node, 0);
        }

        for (String node : subGraph) {
            Set<String> pSet = prerequisites.getOrDefault(node, Collections.emptySet());
            for (String p : pSet) {
                if (subGraph.contains(p)) {
                    inDegree.put(node, inDegree.get(node) + 1);
                }
            }
        }

        // PriorityQueue for deterministic (alphabetical) tie-breaking
        PriorityQueue<String> zeroInDegreeQueue = new PriorityQueue<>();
        for (String node : subGraph) {
            if (inDegree.get(node) == 0) {
                zeroInDegreeQueue.add(node);
            }
        }

        List<String> order = new ArrayList<>();
        while (!zeroInDegreeQueue.isEmpty()) {
            String u = zeroInDegreeQueue.poll();
            order.add(u);

            // For each dependent of u within subGraph
            Set<String> deps = dependents.getOrDefault(u, Collections.emptySet());
            for (String v : deps) {
                if (subGraph.contains(v)) {
                    int updatedDegree = inDegree.get(v) - 1;
                    inDegree.put(v, updatedDegree);
                    if (updatedDegree == 0) {
                        zeroInDegreeQueue.add(v);
                    }
                }
            }
        }

        if (order.size() != subGraph.size()) {
            throw new IllegalStateException("Circular dependency detected during topological sort");
        }

        return order;
    }
}
