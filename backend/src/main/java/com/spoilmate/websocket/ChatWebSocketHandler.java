package com.spoilmate.websocket;

import com.spoilmate.model.ChatMessage;
import com.spoilmate.model.MessageType;
import com.spoilmate.model.Relationship;
import com.spoilmate.service.ChatService;
import com.spoilmate.service.RelationshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final RelationshipService relationshipService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        System.out.println("=== WebSocket sendMessage called (deprecated) ===");
        System.out.println("Request content: " + request.getContent());
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        
        // 这个方法保留以保证向后兼容，但不再做任何事情
        System.out.println("Deprecated: Use /chat.notify instead");
    }
    
    @MessageMapping("/chat.notify")
    public void notifyMessage(@Payload ChatMessageNotification notification, Principal principal) {
        System.out.println("=== WebSocket notifyMessage called ===");
        System.out.println("Message ID: " + notification.getMessageId());
        System.out.println("Principal: " + (principal != null ? principal.getName() : "null"));
        System.out.println("Thread: " + Thread.currentThread().getName());
        
        if (principal == null) {
            System.err.println("Principal is null in notifyMessage");
            return;
        }
        
        try {
            // 从 Principal 中获取用户名
            String username = principal.getName();
            System.out.println("WebSocket notification from user: " + username);
            
            // 获取保存到数据库的消息
            ChatMessage message = chatService.getMessageById(notification.getMessageId());
            if (message == null) {
                System.err.println("Message not found with ID: " + notification.getMessageId());
                return;
            }
            
            System.out.println("Found message with ID: " + message.getId());
            System.out.println("Message content: " + message.getContent());
            System.out.println("Message sender: " + message.getSender().getUsername());
            
            Relationship relationship = relationshipService.getCurrentRelationshipByUsername(username);
            if (relationship == null) {
                System.err.println("No relationship found for user: " + username);
                return;
            }
            System.out.println("Found relationship: " + relationship.getId());

            // 获取伴侣ID
            Long partnerId = relationship.getUser().getUsername().equals(username)
                    ? relationship.getPartner().getId()
                    : relationship.getUser().getId();
            
            String partnerUsername = relationship.getUser().getUsername().equals(username)
                    ? relationship.getPartner().getUsername()
                    : relationship.getUser().getUsername();
            
            System.out.println("Partner ID: " + partnerId);
            System.out.println("Partner Username: " + partnerUsername);
            System.out.println("Sender ID: " + message.getSender().getId());

            // 向伴侣发送完整的消息对象，实现立即显示
            System.out.println("Sending complete message to partner via WebSocket...");
            messagingTemplate.convertAndSendToUser(
                    partnerId.toString(),
                    "/queue/messages", 
                    message
            );
            System.out.println("Complete message sent to partner: " + partnerId);
            System.out.println("Message content sent: " + message.getContent());
            System.out.println("WebSocket message delivery completed");
            
        } catch (Exception e) {
            System.err.println("Error in WebSocket notifyMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(Principal principal) {
        if (principal == null) {
            System.err.println("Principal is null in typing");
            return;
        }
        
        try {
            String username = principal.getName();
            Relationship relationship = relationshipService.getCurrentRelationshipByUsername(username);
            if (relationship != null) {
                Long partnerId = relationship.getUser().getUsername().equals(username)
                        ? relationship.getPartner().getId()
                        : relationship.getUser().getId();

                messagingTemplate.convertAndSendToUser(
                        partnerId.toString(),
                        "/queue/typing",
                        true
                );
            }
        } catch (Exception e) {
            System.err.println("Error in typing: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 