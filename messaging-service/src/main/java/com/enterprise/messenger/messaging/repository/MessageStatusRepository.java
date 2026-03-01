package com.enterprise.messenger.messaging.repository;

import com.enterprise.messenger.common.entity.messaging.MessageStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatusEntity, UUID> {

    Optional<MessageStatusEntity> findByMessageIdAndUserId(UUID messageId, UUID userId);

    List<MessageStatusEntity> findByMessageId(UUID messageId);

    /**
     * Mark all messages in a conversation as read for a user.
     */
    @Modifying
    @Query("UPDATE MessageStatusEntity ms SET ms.status = 'READ', ms.readAt = :readAt " +
           "WHERE ms.userId = :userId " +
           "AND ms.message.conversation.id = :conversationId " +
           "AND ms.status <> 'READ'")
    int markConversationAsRead(@Param("conversationId") UUID conversationId,
                               @Param("userId") UUID userId,
                               @Param("readAt") Instant readAt);

    /**
     * Mark all messages in a conversation as delivered for a user.
     */
    @Modifying
    @Query("UPDATE MessageStatusEntity ms SET ms.status = 'DELIVERED', ms.deliveredAt = :deliveredAt " +
           "WHERE ms.userId = :userId " +
           "AND ms.message.conversation.id = :conversationId " +
           "AND ms.status = 'SENT'")
    int markConversationAsDelivered(@Param("conversationId") UUID conversationId,
                                    @Param("userId") UUID userId,
                                    @Param("deliveredAt") Instant deliveredAt);
}
