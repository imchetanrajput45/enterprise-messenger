package com.enterprise.messenger.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.enterprise.messenger.common.entity.messaging.ConversationEntity;
import com.enterprise.messenger.common.entity.messaging.ConversationMemberEntity;
import com.enterprise.messenger.common.entity.messaging.MessageEntity;
import com.enterprise.messenger.common.entity.messaging.MessageStatusEntity;

@SpringBootApplication(scanBasePackages = {
        "com.enterprise.messenger.messaging",
        "com.enterprise.messenger.common.config",
        "com.enterprise.messenger.common.exception",
        "com.enterprise.messenger.common.security"
})
@EnableDiscoveryClient
@EntityScan(basePackageClasses = {ConversationEntity.class, ConversationMemberEntity.class, MessageEntity.class, MessageStatusEntity.class})
@EnableJpaRepositories(basePackages = "com.enterprise.messenger.messaging.repository")
public class MessagingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagingServiceApplication.class, args);
    }
}
