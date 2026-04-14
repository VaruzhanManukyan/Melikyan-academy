package com.melikyan.academy.config;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.enums.Role;
import org.springframework.boot.ApplicationRunner;
import com.melikyan.academy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Bean
    public ApplicationRunner seedAdmin() {
        return args -> {
            if (!adminProperties.seedEnabled()) {
                System.out.println("Admin seeding disabled");
                return;
            }

            if (adminProperties.email() == null || adminProperties.email().isBlank()) {
                throw new IllegalStateException("APP_ADMIN_EMAIL is required when admin seeding is enabled");
            }

            if (adminProperties.password() == null || adminProperties.password().isBlank()) {
                throw new IllegalStateException("APP_ADMIN_PASSWORD is required when admin seeding is enabled");
            }

            if (adminProperties.firstName() == null || adminProperties.firstName().isBlank()) {
                throw new IllegalStateException("APP_ADMIN_FIRST_NAME is required when admin seeding is enabled");
            }

            if (adminProperties.lastName() == null || adminProperties.lastName().isBlank()) {
                throw new IllegalStateException("APP_ADMIN_LAST_NAME is required when admin seeding is enabled");
            }

            if (userRepository.existsByEmail(adminProperties.email())) {
                System.out.println("Admin already exists");
                return;
            }

            User admin = new User();
            admin.setEmail(adminProperties.email());
            admin.setPassword(passwordEncoder.encode(adminProperties.password()));
            admin.setFirstName(adminProperties.firstName());
            admin.setLastName(adminProperties.lastName());
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        };
    }
}