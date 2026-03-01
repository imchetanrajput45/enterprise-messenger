package com.enterprise.messenger.user.service;

import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.user.ContactDto;

import java.util.UUID;

public interface ContactService {

    PagedResponse<ContactDto> getContacts(UUID ownerId, int page, int size);

    PagedResponse<ContactDto> getFavorites(UUID ownerId, int page, int size);

    ContactDto addContact(UUID ownerId, ContactDto dto);

    ContactDto updateContact(UUID ownerId, UUID contactId, ContactDto dto);

    void removeContact(UUID ownerId, UUID contactId);

    void blockContact(UUID ownerId, UUID contactId);

    void unblockContact(UUID ownerId, UUID contactId);
}
