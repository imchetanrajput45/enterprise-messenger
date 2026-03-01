package com.enterprise.messenger.user.repository;

import com.enterprise.messenger.common.entity.user.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, UUID> {

    Optional<UserProfileEntity> findByAuthUserId(UUID authUserId);

    boolean existsByAuthUserId(UUID authUserId);
}
