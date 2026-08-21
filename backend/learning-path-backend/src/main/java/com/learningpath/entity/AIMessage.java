package com.learningpath.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ai_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AIConversation conversation;

    @Column(name = "role", nullable = false)
    private String role; // "user" or "mentor"

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "topic")
    private String topic;

    @Column(name = "recommended_action")
    private String recommendedAction;
}
