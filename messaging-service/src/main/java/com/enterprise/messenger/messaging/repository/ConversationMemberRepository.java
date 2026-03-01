package com.enterprise.messenger.messaging.repository;

import com.enterprise.messenger.common.entity.messaging.ConversationMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMemberEntity, UUID> {

    List<ConversationMemberEntity> findByConversationId(UUID conversationId);

    Optional<ConversationMemberEntity> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);
}
