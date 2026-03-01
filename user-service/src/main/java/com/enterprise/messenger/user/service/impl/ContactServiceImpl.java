package com.enterprise.messenger.user.service.impl;

import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.user.ContactDto;
import com.enterprise.messenger.common.entity.user.ContactEntity;
import com.enterprise.messenger.common.exception.BadRequestException;
import com.enterprise.messenger.common.exception.ResourceNotFoundException;
import com.enterprise.messenger.user.repository.ContactRepository;
import com.enterprise.messenger.user.repository.UserProfileRepository;
import com.enterprise.messenger.user.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserProfileRepository profileRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContactDto> getContacts(UUID ownerId, int page, int size) {
        Page<ContactEntity> contacts = contactRepository.findByOwnerIdAndBlockedFalse(
                ownerId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return toPagedResponse(contacts);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContactDto> getFavorites(UUID ownerId, int page, int size) {
        Page<ContactEntity> favorites = contactRepository.findByOwnerIdAndFavoriteTrue(
                ownerId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return toPagedResponse(favorites);
    }

    @Override
    @Transactional
    public ContactDto addContact(UUID ownerId, ContactDto dto) {
        if (ownerId.equals(dto.getContactId())) {
            throw new BadRequestException("Cannot add yourself as a contact");
        }
        if (contactRepository.existsByOwnerIdAndContactId(ownerId, dto.getContactId())) {
            throw new BadRequestException("Contact already exists");
        }

        ContactEntity contact = ContactEntity.builder()
                .ownerId(ownerId)
                .contactId(dto.getContactId())
                .nickname(dto.getNickname())
                .build();

        contact = contactRepository.save(contact);
        log.info("Contact added: {} -> {}", ownerId, dto.getContactId());
        return toDto(contact);
    }

    @Override
    @Transactional
    public ContactDto updateContact(UUID ownerId, UUID contactId, ContactDto dto) {
        ContactEntity contact = findContact(ownerId, contactId);
        if (dto.getNickname() != null) contact.setNickname(dto.getNickname());
        if (dto.isFavorite() != contact.isFavorite()) contact.setFavorite(dto.isFavorite());
        contact = contactRepository.save(contact);
        return toDto(contact);
    }

    @Override
    @Transactional
    public void removeContact(UUID ownerId, UUID contactId) {
        ContactEntity contact = findContact(ownerId, contactId);
        contactRepository.delete(contact);
        log.info("Contact removed: {} -> {}", ownerId, contactId);
    }

    @Override
    @Transactional
    public void blockContact(UUID ownerId, UUID contactId) {
        ContactEntity contact = findContact(ownerId, contactId);
        contact.setBlocked(true);
        contactRepository.save(contact);
        log.info("Contact blocked: {} -> {}", ownerId, contactId);
    }

    @Override
    @Transactional
    public void unblockContact(UUID ownerId, UUID contactId) {
        ContactEntity contact = findContact(ownerId, contactId);
        contact.setBlocked(false);
        contactRepository.save(contact);
        log.info("Contact unblocked: {} -> {}", ownerId, contactId);
    }

    private ContactEntity findContact(UUID ownerId, UUID contactId) {
        return contactRepository.findByOwnerIdAndContactId(ownerId, contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", "contactId", contactId));
    }

    private ContactDto toDto(ContactEntity entity) {
        ContactDto dto = ContactDto.builder()
                .id(entity.getId())
                .contactId(entity.getContactId())
                .nickname(entity.getNickname())
                .blocked(entity.isBlocked())
                .favorite(entity.isFavorite())
                .build();

        profileRepository.findByAuthUserId(entity.getContactId())
                .ifPresent(profile -> {
                    dto.setDisplayName(profile.getDisplayName());
                    dto.setAvatarUrl(profile.getAvatarUrl());
                });

        return dto;
    }

    private PagedResponse<ContactDto> toPagedResponse(Page<ContactEntity> page) {
        return PagedResponse.of(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
