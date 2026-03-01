package com.enterprise.messenger.messaging.service.impl;

import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.messaging.ChatMessageEvent;
import com.enterprise.messenger.common.dto.messaging.MessageDto;
import com.enterprise.messenger.common.dto.messaging.SendMessageRequest;
import com.enterprise.messenger.common.entity.messaging.ConversationEntity;
import com.enterprise.messenger.common.entity.messaging.ConversationMemberEntity;
import com.enterprise.messenger.common.entity.messaging.MessageEntity;
import com.enterprise.messenger.common.entity.messaging.MessageStatusEntity;
import com.enterprise.messenger.common.exception.BadRequestException;
import com.enterprise.messenger.common.exception.ResourceNotFoundException;
import com.enterprise.messenger.messaging.kafka.MessageKafkaProducer;
import com.enterprise.messenger.messaging.repository.ConversationMemberRepository;
import com.enterprise.messenger.messaging.repository.ConversationRepository;
import com.enterprise.messenger.messaging.repository.MessageRepository;
import com.enterprise.messenger.messaging.repository.MessageStatusRepository;
import com.enterprise.messenger.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final MessageKafkaProducer kafkaProducer;

    @Override
    @Transactional
    public MessageDto sendMessage(UUID senderId, SendMessageRequest request) {
        ConversationEntity conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", request.getConversationId()));

        // Verify sender is a member
        if (!memberRepository.existsByConversationIdAndUserId(request.getConversationId(), senderId)) {
            throw new BadRequestException("You are not a member of this conversation");
        }

        // Resolve message type
        MessageEntity.MessageType messageType;
        try {
            messageType = request.getMessageType() != null
                    ? MessageEntity.MessageType.valueOf(request.getMessageType().toUpperCase())
                    : MessageEntity.MessageType.TEXT;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid message type: " + request.getMessageType());
        }

        // Create and save message
        MessageEntity message = MessageEntity.builder()
                .conversation(conversation)
                .senderId(senderId)
                .messageType(messageType)
                .content(request.getContent())
                .mediaUrl(request.getMediaUrl())
                .mediaType(request.getMediaType())
                .replyToId(request.getReplyToId())
                .build();

        message = messageRepository.save(message);

        // Update conversation timestamp
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        // Create message status for all other members (SENT)
        List<ConversationMemberEntity> members = memberRepository.findByConversationId(request.getConversationId());
        for (ConversationMemberEntity member : members) {
            if (!member.getUserId().equals(senderId)) {
                MessageStatusEntity status = MessageStatusEntity.builder()
                        .message(message)
                        .userId(member.getUserId())
                        .status(MessageStatusEntity.Status.SENT)
                        .build();
                messageStatusRepository.save(status);
            }
        }

        MessageDto dto = toDto(message);

        // Publish Kafka event for real-time delivery
        ChatMessageEvent event = ChatMessageEvent.builder()
                .messageId(message.getId())
                .conversationId(conversation.getId())
                .senderId(senderId)
                .messageType(messageType.name())
                .content(message.getContent())
                .mediaUrl(message.getMediaUrl())
                .replyToId(message.getReplyToId())
                .timestamp(message.getCreatedAt())
                .eventType("SENT")
                .build();

        kafkaProducer.publishMessageSent(event);

        log.info("Message sent: {} in conversation {}", message.getId(), conversation.getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MessageDto> getMessages(UUID userId, UUID conversationId, int page, int size) {
        // Verify membership
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new BadRequestException("You are not a member of this conversation");
        }

        Page<MessageEntity> messages = messageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, PageRequest.of(page, size));

        List<MessageDto> dtos = messages.getContent().stream()
                .map(msg -> {
                    MessageDto dto = toDto(msg);
                    if (msg.getSenderId().equals(userId)) {
                        // User sent this message, check if it was read/delivered by others
                        List<MessageStatusEntity> statuses = messageStatusRepository.findByMessageId(msg.getId());
                        boolean allRead = !statuses.isEmpty() && statuses.stream().allMatch(s -> s.getStatus() == MessageStatusEntity.Status.READ);
                        boolean anyDelivered = statuses.stream().anyMatch(s -> s.getStatus() == MessageStatusEntity.Status.DELIVERED || s.getStatus() == MessageStatusEntity.Status.READ);
                        
                        if (allRead) {
                            dto.setLocalStatus("READ");
                        } else if (anyDelivered) {
                            dto.setLocalStatus("DELIVERED");
                        } else {
                            dto.setLocalStatus("SENT");
                        }
                    }
                    return dto;
                })
                .toList();

        return PagedResponse.of(dtos, page, size, messages.getTotalElements(),
                messages.getTotalPages(), messages.isLast());
    }

    @Override
    @Transactional
    public void markAsRead(UUID userId, UUID conversationId) {
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new BadRequestException("You are not a member of this conversation");
        }

        int updated = messageStatusRepository.markConversationAsRead(conversationId, userId, Instant.now());
        log.debug("Marked {} messages as read in conversation {} for user {}", updated, conversationId, userId);

        if (updated > 0) {
            ChatMessageEvent event = ChatMessageEvent.builder()
                    .conversationId(conversationId)
                    .senderId(userId)
                    .timestamp(Instant.now())
                    .eventType("READ")
                    .build();
            kafkaProducer.publishMessageRead(event);
        }
    }

    @Override
    @Transactional
    public void markAsDelivered(UUID userId, UUID conversationId) {
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            return; // Silently ignore for non-members
        }

        int updated = messageStatusRepository.markConversationAsDelivered(conversationId, userId, Instant.now());
        log.debug("Marked {} messages as delivered in conversation {} for user {}", updated, conversationId, userId);

        if (updated > 0) {
            ChatMessageEvent event = ChatMessageEvent.builder()
                    .conversationId(conversationId)
                    .senderId(userId)
                    .timestamp(Instant.now())
                    .eventType("DELIVERED")
                    .build();
            kafkaProducer.publishMessageDelivered(event);
        }
    }

    private MessageDto toDto(MessageEntity entity) {
        return MessageDto.builder()
                .id(entity.getId())
                .conversationId(entity.getConversation().getId())
                .senderId(entity.getSenderId())
                .messageType(entity.getMessageType().name())
                .content(entity.getContent())
                .mediaUrl(entity.getMediaUrl())
                .mediaType(entity.getMediaType())
                .replyToId(entity.getReplyToId())
                .edited(entity.isEdited())
                .deleted(entity.isDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
