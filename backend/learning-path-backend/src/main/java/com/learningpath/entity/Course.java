package com.learningpath.entity;

import com.learningpath.entity.enums.CourseDifficulty;
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
                @Index(name = "idx_courses_difficulty", columnList = "difficulty")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseDifficulty difficulty;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
}
