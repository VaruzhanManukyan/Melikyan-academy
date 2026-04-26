package com.melikyan.academy.dto.response.professor;

import com.melikyan.academy.entity.enums.Role;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ProfessorUserData(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
