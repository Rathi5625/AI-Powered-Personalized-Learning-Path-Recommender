package com.learningpath.learningpath.service;

import com.learningpath.entity.UserProgress;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A snapshot of the learner's current effective state, computed deterministically
 * from the database. Never populated by AI output.
 *
 * @param userId           The learner's unique identifier.
 * @param careerId         The target career the learner is pursuing.
 * @param targetCareer     The human-readable career title.
 * @param completedSkills  Skills the learner has already mastered (ADVANCED/EXPERT proficiency
 *                         or all associated courses COMPLETED).
 * @param remainingSkills  Skills in the career's skill gap that have not yet been mastered.
 * @param courseProgress   Raw UserProgress records for the learner's enrolled courses.
 */
public record LearnerSnapshot(
        UUID userId,
        UUID careerId,
        String targetCareer,
        Set<String> completedSkills,
        Set<String> remainingSkills,
        List<UserProgress> courseProgress
) {}
