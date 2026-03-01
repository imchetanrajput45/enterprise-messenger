package com.enterprise.messenger.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.enterprise.messenger.common.entity.auth.UserEntity;
import com.enterprise.messenger.common.entity.auth.RoleEntity;

@SpringBootApplication(scanBasePackages = {
        "com.enterprise.messenger.auth",
        "com.enterprise.messenger.common.config",
        "com.enterprise.messenger.common.exception",
        "com.enterprise.messenger.common.security"
})
@EnableDiscoveryClient
@EntityScan(basePackageClasses = {UserEntity.class, RoleEntity.class})
@EnableJpaRepositories(basePackages = "com.enterprise.messenger.auth.repository")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
