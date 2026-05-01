package com.melikyan.academy.dto.response.category;

import java.util.UUID;
import java.time.OffsetDateTime;

public record CategoryResponse(
        UUID id,
        String title,
        String description,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
