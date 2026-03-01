package com.enterprise.messenger.messaging.service;

import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.messaging.ConversationDto;
import com.enterprise.messenger.common.dto.messaging.CreateConversationRequest;

import java.util.UUID;

public interface ConversationService {

    ConversationDto createConversation(UUID userId, CreateConversationRequest request);

    PagedResponse<ConversationDto> getConversations(UUID userId, int page, int size);

    ConversationDto getConversation(UUID userId, UUID conversationId);
}
