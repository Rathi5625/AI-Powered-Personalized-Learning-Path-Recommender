package com.learningpath.entity;

import com.learningpath.entity.enums.CourseDifficulty;
import com.learningpath.entity.enums.CourseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_courses_title", columnList = "title"),
                @Index(name = "idx_courses_provider", columnList = "provider"),
                @Index(name = "idx_courses_difficulty", columnList = "difficulty"),
                @Index(name = "idx_courses_course_type", columnList = "course_type"),
                @Index(name = "idx_courses_is_free", columnList = "is_free")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String provider;

    @Column(length = 500)
    private String url;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "duration_hours")
    private Double durationHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", length = 30)
    private CourseType courseType;

    @Column(length = 50)
    @Builder.Default
    private String language = "English";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseDifficulty difficulty;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_free", nullable = false)
    @Builder.Default
    private boolean isFree = false;
}
