package com.learningpath.repository;

import com.learningpath.entity.Course;
import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    public static Specification<Course> filterCourses(
            CourseDifficulty difficulty,
            CourseType courseType,
            String provider,
            Boolean isFree,
            String language
    ) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (difficulty != null) {
                predicate = cb.and(predicate, cb.equal(root.get("difficulty"), difficulty));
            }
            if (courseType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("courseType"), courseType));
            }
            if (provider != null && !provider.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("provider")), "%" + provider.toLowerCase() + "%"));
            }
            if (isFree != null) {
                predicate = cb.and(predicate, cb.equal(root.get("isFree"), isFree));
            }
            if (language != null && !language.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("language")), "%" + language.toLowerCase() + "%"));
            }

            return predicate;
        };
    }
}
