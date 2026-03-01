package com.enterprise.messenger.messaging.repository;

import com.enterprise.messenger.common.entity.messaging.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    /**
     * Find all conversations a user belongs to, ordered by most recent activity.
     */
    @Query("SELECT c FROM ConversationEntity c " +
           "JOIN ConversationMemberEntity cm ON cm.conversation.id = c.id " +
           "WHERE cm.userId = :userId " +
           "ORDER BY c.updatedAt DESC")
    Page<ConversationEntity> findByMemberUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find existing DIRECT conversation between two users.
     */
    @Query("SELECT c FROM ConversationEntity c " +
           "WHERE c.type = 'DIRECT' " +
           "AND c.id IN (SELECT cm1.conversation.id FROM ConversationMemberEntity cm1 WHERE cm1.userId = :userId1) " +
           "AND c.id IN (SELECT cm2.conversation.id FROM ConversationMemberEntity cm2 WHERE cm2.userId = :userId2)")
    java.util.Optional<ConversationEntity> findDirectConversation(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2);
}
