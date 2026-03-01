package com.enterprise.messenger.messaging.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.enterprise.messenger.common.config.KafkaTopics.*;

/**
 * Kafka topic auto-creation for messaging events.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic messageSentTopic() {
        return new NewTopic(MESSAGE_SENT, 3, (short) 1);
    }

    @Bean
    public NewTopic messageDeliveredTopic() {
        return new NewTopic(MESSAGE_DELIVERED, 3, (short) 1);
    }

    @Bean
    public NewTopic messageReadTopic() {
        return new NewTopic(MESSAGE_READ, 3, (short) 1);
    }
}
