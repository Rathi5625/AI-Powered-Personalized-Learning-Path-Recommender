package com.learningpath.entity;

import com.learningpath.entity.enums.RecommendationInteractionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "recommendation_interactions",
        indexes = {
                @Index(name = "idx_rec_interactions_user_id", columnList = "user_id"),
                @Index(name = "idx_rec_interactions_course_id", columnList = "course_id"),
                @Index(name = "idx_rec_interactions_interaction_type", columnList = "interaction_type"),
                @Index(name = "idx_rec_interactions_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationInteraction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 30)
    private RecommendationInteractionType interactionType;

    @Column(name = "recommendation_rank")
    private Integer recommendationRank;

    @Column(name = "rule_based_score")
    private Double ruleBasedScore;

    @Column(name = "ml_score")
    private Double mlScore;

    @Column(name = "final_score", nullable = false)
    private Double finalScore;
}
