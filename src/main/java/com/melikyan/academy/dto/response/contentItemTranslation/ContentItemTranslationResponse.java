package com.melikyan.academy.dto.response.contentItemTranslation;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ContentItemTranslationResponse(
        UUID id,
        String code,
        String title,
        String description,
        UUID contentItemId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}