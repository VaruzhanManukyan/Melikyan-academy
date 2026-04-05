package com.melikyan.academy.dto.response.user;

import com.melikyan.academy.entity.enums.Role;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String avatarUrl,
        String bio,
        Role role,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
