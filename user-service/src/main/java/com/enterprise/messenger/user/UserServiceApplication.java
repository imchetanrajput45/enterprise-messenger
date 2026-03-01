package com.enterprise.messenger.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.enterprise.messenger.common.entity.user.UserProfileEntity;
import com.enterprise.messenger.common.entity.user.ContactEntity;

@SpringBootApplication(scanBasePackages = {
        "com.enterprise.messenger.user",
        "com.enterprise.messenger.common.config",
        "com.enterprise.messenger.common.exception",
        "com.enterprise.messenger.common.security"
})
@EnableDiscoveryClient
@EntityScan(basePackageClasses = {UserProfileEntity.class, ContactEntity.class})
@EnableJpaRepositories(basePackages = "com.enterprise.messenger.user.repository")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
