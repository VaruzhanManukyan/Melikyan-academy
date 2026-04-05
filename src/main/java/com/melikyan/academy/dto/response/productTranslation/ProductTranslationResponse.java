package com.melikyan.academy.dto.response.productTranslation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductTranslationResponse(
        UUID id,
        String title,
        String description,
        String code,
        UUID productId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
