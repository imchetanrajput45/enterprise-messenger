package com.enterprise.messenger.messaging.controller;

import com.enterprise.messenger.common.dto.ApiResponse;
import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.messaging.ConversationDto;
import com.enterprise.messenger.common.dto.messaging.CreateConversationRequest;
import com.enterprise.messenger.messaging.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationDto>> createConversation(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateConversationRequest request) {
        ConversationDto conversation = conversationService.createConversation(UUID.fromString(userId), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Conversation created", conversation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ConversationDto>>> getConversations(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ConversationDto> conversations = conversationService.getConversations(
                UUID.fromString(userId), page, size);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationDto>> getConversation(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID conversationId) {
        ConversationDto conversation = conversationService.getConversation(
                UUID.fromString(userId), conversationId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }
}
