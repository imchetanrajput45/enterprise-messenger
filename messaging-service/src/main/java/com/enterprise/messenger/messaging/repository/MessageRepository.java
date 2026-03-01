package com.enterprise.messenger.messaging.repository;

import com.enterprise.messenger.common.entity.messaging.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    Page<MessageEntity> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    /**
     * Find the most recent message in a conversation (for last message preview).
     */
    Optional<MessageEntity> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    /**
     * Count messages in a conversation after a given message ID (for unread count).
     */
    @Query("SELECT COUNT(m) FROM MessageEntity m " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.senderId <> :userId " +
           "AND m.id NOT IN " +
           "(SELECT ms.message.id FROM MessageStatusEntity ms WHERE ms.userId = :userId AND ms.status = 'READ')")
    long countUnreadMessages(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}
