package com.learningpath.repository;

import com.learningpath.entity.AIConversation;
import com.learningpath.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AIConversationRepository extends JpaRepository<AIConversation, UUID> {
    List<AIConversation> findByUserOrderByCreatedAtDesc(User user);
    Optional<AIConversation> findFirstByUserOrderByCreatedAtDesc(User user);
    void deleteByUser(User user);
}
