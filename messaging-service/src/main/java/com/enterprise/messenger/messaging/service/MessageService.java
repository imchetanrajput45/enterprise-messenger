package com.enterprise.messenger.messaging.service;

import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.messaging.MessageDto;
import com.enterprise.messenger.common.dto.messaging.SendMessageRequest;

import java.util.UUID;

public interface MessageService {

    MessageDto sendMessage(UUID senderId, SendMessageRequest request);

    PagedResponse<MessageDto> getMessages(UUID userId, UUID conversationId, int page, int size);

    void markAsRead(UUID userId, UUID conversationId);

    void markAsDelivered(UUID userId, UUID conversationId);
}
