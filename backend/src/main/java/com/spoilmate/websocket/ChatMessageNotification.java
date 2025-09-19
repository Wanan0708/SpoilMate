package com.spoilmate.websocket;

import lombok.Data;

@Data
public class ChatMessageNotification {
    private Long messageId;
    
    public ChatMessageNotification() {}
    
    public ChatMessageNotification(Long messageId) {
        this.messageId = messageId;
    }
}