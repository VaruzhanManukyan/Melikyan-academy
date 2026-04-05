package com.melikyan.academy.dto.response.professor;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ProfessorResponse(
        UUID id,
        UUID userId,
        UUID courseId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
