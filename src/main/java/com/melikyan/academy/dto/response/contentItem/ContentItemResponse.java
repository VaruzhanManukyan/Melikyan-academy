package com.melikyan.academy.dto.response.contentItem;

import com.melikyan.academy.entity.enums.PurchasableType;
import com.melikyan.academy.dto.response.category.CategoryShortResponse;

import java.util.UUID;
import java.time.OffsetDateTime;

public record Response(
        UUID id,
        String title,
        String description,
        PurchasableType type,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
