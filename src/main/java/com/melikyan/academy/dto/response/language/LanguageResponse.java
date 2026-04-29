package com.melikyan.academy.dto.response.language;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LanguageResponse(
        UUID id,
        String code,
        String name,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
