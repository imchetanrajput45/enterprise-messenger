package com.enterprise.messenger.user.repository;

import com.enterprise.messenger.common.entity.user.ContactEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntity, UUID> {

    Page<ContactEntity> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<ContactEntity> findByOwnerIdAndBlockedFalse(UUID ownerId, Pageable pageable);

    Page<ContactEntity> findByOwnerIdAndFavoriteTrue(UUID ownerId, Pageable pageable);

    Optional<ContactEntity> findByOwnerIdAndContactId(UUID ownerId, UUID contactId);

    boolean existsByOwnerIdAndContactId(UUID ownerId, UUID contactId);
}
