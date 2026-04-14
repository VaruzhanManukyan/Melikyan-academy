package com.melikyan.academy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        boolean seedEnabled,
        String email,
        String password,
        String firstName,
        String lastName
) {
}
