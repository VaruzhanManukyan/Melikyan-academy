package com.melikyan.academy.dto.response.contentItemTranslation;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ContentItemTranslationResponse(
        UUID id,
        String title,
        String description,
        String code,
        UUID contentItemId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}