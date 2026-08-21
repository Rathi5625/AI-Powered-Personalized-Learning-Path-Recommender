package com.learningpath.repository;

import com.learningpath.entity.AIConversation;
import com.learningpath.entity.AIMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AIMessageRepository extends JpaRepository<AIMessage, UUID> {
    List<AIMessage> findByConversationOrderByCreatedAtAsc(AIConversation conversation);
}
