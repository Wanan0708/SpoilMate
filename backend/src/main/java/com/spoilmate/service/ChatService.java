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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RelationshipService relationshipService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessage sendMessage(String content, MessageType type) {
        System.out.println("=== ChatService.sendMessage called ==="); 
        System.out.println("Content: " + content);
        System.out.println("Type: " + type);
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Sender username: " + username);
        
        ChatMessage savedMessage = sendMessageByUsername(content, type, username);
        System.out.println("Message saved with ID: " + savedMessage.getId());
        
        // 立即触发WebSocket推送
        triggerWebSocketPush(savedMessage, username);
        
        return savedMessage;
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
    
    private void triggerWebSocketPush(ChatMessage message, String senderUsername) {
        try {
            System.out.println("=== Triggering WebSocket Push ===");
            System.out.println("Message ID: " + message.getId());
            System.out.println("Sender: " + senderUsername);
            
            Relationship relationship = relationshipService.getCurrentRelationshipByUsername(senderUsername);
            if (relationship == null) {
                System.err.println("No relationship found for WebSocket push");
                return;
            }
            
            // 获取伴侣ID
            User partner;
            if (relationship.getUser().getUsername().equals(senderUsername)) {
                partner = relationship.getPartner();
            } else {
                partner = relationship.getUser();
            }
            
            Long partnerId = partner.getId();
            String partnerUsername = partner.getUsername();
            
            System.out.println("Sending WebSocket message to partner: " + partnerUsername + " (ID: " + partnerId + ")");
            
            // 向伴侣发送完整消息 - 使用用户名而不是数据库ID
            messagingTemplate.convertAndSendToUser(
                    partnerUsername,  // 使用用户名
                    "/queue/messages",
                    message
            );
            
            System.out.println("WebSocket message sent successfully to partner using username: " + partnerUsername);
            
            // 为了测试，也向发送者发送一份副本（便于调试）
            User sender = message.getSender();
            System.out.println("Also sending to sender for testing: " + sender.getUsername() + " (ID: " + sender.getId() + ")");
            messagingTemplate.convertAndSendToUser(
                    sender.getUsername(),  // 使用用户名
                    "/queue/messages",
                    message
            );
            System.out.println("Test message also sent to sender using username: " + sender.getUsername());
            
        } catch (Exception e) {
            System.err.println("Error in WebSocket push: " + e.getMessage());
            e.printStackTrace();
        }
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