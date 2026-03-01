package com.enterprise.messenger.common.config;

/**
 * Central registry of all Kafka topic names.
 * Ensures consistency across producers and consumers in all microservices.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class — no instantiation
    }

    // ─── Messaging ─────────────────────────────────────────────
    public static final String MESSAGE_SENT      = "messenger.message.sent";
    public static final String MESSAGE_DELIVERED  = "messenger.message.delivered";
    public static final String MESSAGE_READ       = "messenger.message.read";

    // ─── Presence ──────────────────────────────────────────────
    public static final String USER_ONLINE        = "messenger.user.online";
    public static final String USER_OFFLINE       = "messenger.user.offline";
    public static final String USER_TYPING        = "messenger.user.typing";

    // ─── Notifications ─────────────────────────────────────────
    public static final String PUSH_NOTIFICATION  = "messenger.notification.push";

    // ─── User Events ───────────────────────────────────────────
    public static final String USER_REGISTERED    = "messenger.user.registered";
    public static final String USER_UPDATED       = "messenger.user.updated";

    // ─── Audit ─────────────────────────────────────────────────
    public static final String AUDIT_LOG          = "messenger.audit.log";
}
