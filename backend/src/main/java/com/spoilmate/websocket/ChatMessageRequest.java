package com.spoilmate.websocket;

import com.spoilmate.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private String content;
    private MessageType type = MessageType.TEXT;
} 