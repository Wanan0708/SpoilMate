package com.spoilmate.controller;

import com.spoilmate.model.ChatMessage;
import com.spoilmate.model.MessageType;
import com.spoilmate.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestParam String content,
            @RequestParam(defaultValue = "TEXT") MessageType type) {
        return ResponseEntity.ok(chatService.sendMessage(content, type));
    }

    @GetMapping("/messages")
    public ResponseEntity<Page<ChatMessage>> getMessages(Pageable pageable) {
        return ResponseEntity.ok(chatService.getMessages(pageable));
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
        chatService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(chatService.getUnreadCount());
    }
} 