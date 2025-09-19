package com.spoilmate.repository;

import com.spoilmate.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByRelationshipIdOrderByCreatedAtAsc(Long relationshipId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.relationship.id = :relationshipId AND m.read = false AND m.sender.id != :userId")
    long countUnreadMessages(@Param("relationshipId") Long relationshipId, @Param("userId") Long userId);
} 