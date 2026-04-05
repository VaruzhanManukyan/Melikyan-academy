package com.melikyan.academy.dto.response.purchasableTranslation;

import java.util.UUID;
import java.time.OffsetDateTime;

public record PurchasableTranslationResponse(
        UUID id,
        String title,
        String description,
        String code,
        UUID purchasableId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}