package com.enterprise.messenger.messaging.controller;

import com.enterprise.messenger.common.dto.ApiResponse;
import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.messaging.MessageDto;
import com.enterprise.messenger.common.dto.messaging.SendMessageRequest;
import com.enterprise.messenger.messaging.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageDto message = messageService.sendMessage(UUID.fromString(userId), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent", message));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<PagedResponse<MessageDto>>> getMessages(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PagedResponse<MessageDto> messages = messageService.getMessages(
                UUID.fromString(userId), conversationId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID conversationId) {
        messageService.markAsRead(UUID.fromString(userId), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }

    @PutMapping("/{conversationId}/delivered")
    public ResponseEntity<ApiResponse<Void>> markAsDelivered(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID conversationId) {
        messageService.markAsDelivered(UUID.fromString(userId), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as delivered", null));
    }
}
