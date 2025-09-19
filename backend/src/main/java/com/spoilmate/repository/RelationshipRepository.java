package com.spoilmate.repository;

import com.spoilmate.model.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    Optional<Relationship> findByUserId(Long userId);
    Optional<Relationship> findByPartnerId(Long partnerId);
    Optional<Relationship> findByInvitationCode(String invitationCode);
} 