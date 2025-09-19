package com.spoilmate.service;

import com.spoilmate.model.Relationship;
import com.spoilmate.model.RelationshipStatus;
import com.spoilmate.model.User;
import com.spoilmate.repository.RelationshipRepository;
import com.spoilmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    private static final Logger logger = LoggerFactory.getLogger(RelationshipService.class);
    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public Relationship createInvitation() {
        try {
            logger.info("Starting invitation creation process...");
            
            // 1. 获取当前用户
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            logger.debug("Current username: {}", username);
            
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            logger.info("User found: {} (ID: {})", user.getUsername(), user.getId());

            // 2. 检查现有关系
            logger.debug("Checking existing relationships for user ID: {}", user.getId());
            checkExistingRelationships(user);

            // 3. 生成邀请码
            String invitationCode = generateInvitationCode();
            logger.debug("Generated invitation code: {}", invitationCode);

            // 4. 创建新关系
            Relationship relationship = new Relationship();
            relationship.setUser(user);
            relationship.setInvitationCode(invitationCode);
            relationship.setStatus(RelationshipStatus.PENDING);
            relationship.setCreatedAt(LocalDateTime.now());
            relationship.setUpdatedAt(LocalDateTime.now());

            logger.info("Saving new relationship with invitation code: {}", invitationCode);

            // 5. 保存关系
            Relationship savedRelationship = relationshipRepository.save(relationship);
            logger.info("Successfully saved relationship with ID: {}", savedRelationship.getId());
            
            return savedRelationship;
        } catch (Exception e) {
            logger.error("Failed to create invitation", e);
            throw new RuntimeException("Failed to create invitation: " + e.getMessage());
        }
    }

    private void checkExistingRelationships(User user) {
        logger.debug("Checking user relationships...");
        
        relationshipRepository.findByUserId(user.getId())
                .ifPresent(r -> {
                    logger.warn("Found existing relationship as user: {}", r);
                    if (r.getStatus() == RelationshipStatus.ACTIVE) {
                        throw new RuntimeException("You already have an active relationship");
                    }
                    if (r.getStatus() == RelationshipStatus.PENDING) {
                        throw new RuntimeException("You already have a pending invitation");
                    }
                });

        relationshipRepository.findByPartnerId(user.getId())
                .ifPresent(r -> {
                    logger.warn("Found existing relationship as partner: {}", r);
                    if (r.getStatus() == RelationshipStatus.ACTIVE) {
                        throw new RuntimeException("You already have an active relationship");
                    }
                    if (r.getStatus() == RelationshipStatus.PENDING) {
                        throw new RuntimeException("You have a pending invitation to accept");
                    }
                });
        
        logger.debug("Relationship check completed successfully");
    }

    @Transactional
    public Relationship acceptInvitation(String invitationCode) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User partner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 检查用户是否已经在一段关系中
        relationshipRepository.findByUserId(partner.getId())
                .ifPresent(r -> {
                    throw new RuntimeException("User already in a relationship");
                });

        relationshipRepository.findByPartnerId(partner.getId())
                .ifPresent(r -> {
                    throw new RuntimeException("User already in a relationship");
                });

        // 查找并更新关系
        Relationship relationship = relationshipRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new RuntimeException("Invalid invitation code"));

        if (relationship.getStatus() != RelationshipStatus.PENDING) {
            throw new RuntimeException("Invalid invitation status");
        }

        if (relationship.getUser().getId().equals(partner.getId())) {
            throw new RuntimeException("Cannot accept your own invitation");
        }

        relationship.setPartner(partner);
        relationship.setStatus(RelationshipStatus.ACTIVE);
        relationship.setStartDate(LocalDateTime.now());
        relationship.setInvitationCode(null);

        return relationshipRepository.save(relationship);
    }

    @Transactional
    public void endRelationship() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Relationship relationship = relationshipRepository.findByUserId(user.getId())
                .orElse(null);

        if (relationship == null) {
            relationship = relationshipRepository.findByPartnerId(user.getId())
                    .orElseThrow(() -> new RuntimeException("No active relationship found"));
        }

        if (relationship.getStatus() != RelationshipStatus.ACTIVE) {
            throw new RuntimeException("No active relationship found");
        }

        relationship.setStatus(RelationshipStatus.INACTIVE);
        relationshipRepository.save(relationship);
    }

    public Relationship getCurrentRelationship() {
        logger.info("Fetching current relationship...");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getCurrentRelationshipByUsername(username);
    }
    
    public Relationship getCurrentRelationshipByUsername(String username) {
        logger.info("Fetching current relationship for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Relationship relationship = relationshipRepository.findByUserId(user.getId())
                .orElse(null);

        if (relationship == null) {
            relationship = relationshipRepository.findByPartnerId(user.getId())
                    .orElse(null);
        }

        return relationship;
    }

    private String generateInvitationCode() {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        logger.debug("Generated new invitation code: {}", code);
        
        // 检查邀请码是否已存在
        while (relationshipRepository.findByInvitationCode(code).isPresent()) {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            logger.debug("Code already exists, generated new code: {}", code);
        }
        
        return code;
    }
} 