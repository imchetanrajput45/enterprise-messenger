package com.enterprise.messenger.messaging.service.impl;

import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.messaging.ConversationDto;
import com.enterprise.messenger.common.dto.messaging.ConversationMemberDto;
import com.enterprise.messenger.common.dto.messaging.CreateConversationRequest;
import com.enterprise.messenger.common.dto.messaging.MessageDto;
import com.enterprise.messenger.common.entity.messaging.ConversationEntity;
import com.enterprise.messenger.common.entity.messaging.ConversationMemberEntity;
import com.enterprise.messenger.common.entity.messaging.MessageEntity;
import com.enterprise.messenger.common.exception.BadRequestException;
import com.enterprise.messenger.common.exception.ResourceNotFoundException;
import com.enterprise.messenger.messaging.repository.ConversationMemberRepository;
import com.enterprise.messenger.messaging.repository.ConversationRepository;
import com.enterprise.messenger.messaging.repository.MessageRepository;
import com.enterprise.messenger.messaging.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public ConversationDto createConversation(UUID userId, CreateConversationRequest request) {
        ConversationEntity.ConversationType type;
        try {
            type = ConversationEntity.ConversationType.valueOf(
                    request.getType() != null ? request.getType().toUpperCase() : "DIRECT");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid conversation type: " + request.getType());
        }

        // DIRECT: exactly 1 other member, deduplicate existing
        if (type == ConversationEntity.ConversationType.DIRECT) {
            if (request.getMemberIds().size() != 1) {
                throw new BadRequestException("DIRECT conversation requires exactly 1 other member");
            }
            UUID otherUserId = request.getMemberIds().get(0);
            if (otherUserId.equals(userId)) {
                throw new BadRequestException("Cannot create a conversation with yourself");
            }

            // Check if a DIRECT conversation already exists
            return conversationRepository.findDirectConversation(userId, otherUserId)
                    .map(existing -> toDto(existing, userId))
                    .orElseGet(() -> createNewConversation(userId, type, null, null, List.of(otherUserId)));
        }

        // GROUP: at least 1 member, name required
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Group name is required");
        }

        return createNewConversation(userId, type, request.getName(), request.getAvatarUrl(), request.getMemberIds());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ConversationDto> getConversations(UUID userId, int page, int size) {
        Page<ConversationEntity> conversations = conversationRepository.findByMemberUserId(
                userId, PageRequest.of(page, size));

        List<ConversationDto> dtos = conversations.getContent().stream()
                .map(c -> toDto(c, userId))
                .toList();

        return PagedResponse.of(dtos, page, size, conversations.getTotalElements(),
                conversations.getTotalPages(), conversations.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversation(UUID userId, UUID conversationId) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        // Verify user is a member
        if (!memberRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new BadRequestException("You are not a member of this conversation");
        }

        return toDto(conversation, userId);
    }

    // ─── Helpers ───────────────────────────────────────────────

    private ConversationDto createNewConversation(UUID userId, ConversationEntity.ConversationType type,
                                                   String name, String avatarUrl, List<UUID> memberIds) {
        ConversationEntity conversation = ConversationEntity.builder()
                .type(type)
                .name(name)
                .avatarUrl(avatarUrl)
                .createdBy(userId)
                .members(new ArrayList<>())
                .build();

        conversation = conversationRepository.save(conversation);

        // Add creator as OWNER
        ConversationMemberEntity ownerMember = ConversationMemberEntity.builder()
                .conversation(conversation)
                .userId(userId)
                .role(ConversationMemberEntity.MemberRole.OWNER)
                .build();
        memberRepository.save(ownerMember);

        // Add other members
        for (UUID memberId : memberIds) {
            ConversationMemberEntity member = ConversationMemberEntity.builder()
                    .conversation(conversation)
                    .userId(memberId)
                    .role(ConversationMemberEntity.MemberRole.MEMBER)
                    .build();
            memberRepository.save(member);
        }

        log.info("Conversation created: {} (type: {}, members: {})",
                conversation.getId(), type, memberIds.size() + 1);

        return toDto(conversation, userId);
    }

    private ConversationDto toDto(ConversationEntity entity, UUID requestingUserId) {
        List<ConversationMemberDto> members = memberRepository.findByConversationId(entity.getId())
                .stream()
                .map(m -> ConversationMemberDto.builder()
                        .userId(m.getUserId())
                        .role(m.getRole().name())
                        .muted(m.isMuted())
                        .build())
                .toList();

        // Last message
        MessageDto lastMessage = messageRepository
                .findFirstByConversationIdOrderByCreatedAtDesc(entity.getId())
                .map(this::toMessageDto)
                .orElse(null);

        // Unread count
        long unreadCount = messageRepository.countUnreadMessages(entity.getId(), requestingUserId);

        return ConversationDto.builder()
                .id(entity.getId())
                .type(entity.getType().name())
                .name(entity.getName())
                .avatarUrl(entity.getAvatarUrl())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .members(members)
                .build();
    }

    private MessageDto toMessageDto(MessageEntity entity) {
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
