package com.spoilmate.service;

import com.spoilmate.model.ChatMessage;
import com.spoilmate.model.MessageType;
import com.spoilmate.model.Relationship;
import com.spoilmate.model.RelationshipStatus;
import com.spoilmate.model.User;
import com.spoilmate.repository.ChatMessageRepository;
import com.spoilmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RelationshipService relationshipService;

    @Transactional
    public ChatMessage sendMessage(String content, MessageType type) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return sendMessageByUsername(content, type, username);
    }
    
    @Transactional
    public ChatMessage sendMessageByUsername(String content, MessageType type, String username) {
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Relationship relationship = relationshipService.getCurrentRelationshipByUsername(username);
        if (relationship == null || !relationship.getStatus().equals(RelationshipStatus.ACTIVE)) {
            throw new RuntimeException("No active relationship found");
        }

        ChatMessage message = ChatMessage.builder()
                .relationship(relationship)
                .sender(sender)
                .content(content)
                .type(type)
                .read(false)
                .build();

        return chatMessageRepository.save(message);
    }

    public Page<ChatMessage> getMessages(Pageable pageable) {
        Relationship relationship = relationshipService.getCurrentRelationship();
        if (relationship == null) {
            throw new RuntimeException("No relationship found");
        }

        return chatMessageRepository.findByRelationshipIdOrderByCreatedAtAsc(
                relationship.getId(), pageable);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(user.getId())) {
            message.setRead(true);
            chatMessageRepository.save(message);
        }
    }

    public ChatMessage getMessageById(Long messageId) {
        return chatMessageRepository.findById(messageId)
                .orElse(null);
    }
    
    public long getUnreadCount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Relationship relationship = relationshipService.getCurrentRelationship();
        if (relationship == null) {
            return 0;
        }

        return chatMessageRepository.countUnreadMessages(relationship.getId(), user.getId());
    }
} 