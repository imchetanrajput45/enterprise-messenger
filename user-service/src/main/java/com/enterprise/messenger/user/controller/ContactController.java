package com.enterprise.messenger.user.controller;

import com.enterprise.messenger.common.dto.ApiResponse;
import com.enterprise.messenger.common.dto.PagedResponse;
import com.enterprise.messenger.common.dto.user.ContactDto;
import com.enterprise.messenger.user.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ContactDto>>> getContacts(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ContactDto> contacts = contactService.getContacts(UUID.fromString(userId), page, size);
        return ResponseEntity.ok(ApiResponse.success(contacts));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<PagedResponse<ContactDto>>> getFavorites(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ContactDto> favorites = contactService.getFavorites(UUID.fromString(userId), page, size);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContactDto>> addContact(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ContactDto dto) {
        ContactDto contact = contactService.addContact(UUID.fromString(userId), dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contact added", contact));
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<ApiResponse<ContactDto>> updateContact(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID contactId,
            @Valid @RequestBody ContactDto dto) {
        ContactDto contact = contactService.updateContact(UUID.fromString(userId), contactId, dto);
        return ResponseEntity.ok(ApiResponse.success("Contact updated", contact));
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<ApiResponse<Void>> removeContact(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID contactId) {
        contactService.removeContact(UUID.fromString(userId), contactId);
        return ResponseEntity.ok(ApiResponse.success("Contact removed", null));
    }

    @PostMapping("/{contactId}/block")
    public ResponseEntity<ApiResponse<Void>> blockContact(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID contactId) {
        contactService.blockContact(UUID.fromString(userId), contactId);
        return ResponseEntity.ok(ApiResponse.success("Contact blocked", null));
    }

    @PostMapping("/{contactId}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockContact(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID contactId) {
        contactService.unblockContact(UUID.fromString(userId), contactId);
        return ResponseEntity.ok(ApiResponse.success("Contact unblocked", null));
    }
}
