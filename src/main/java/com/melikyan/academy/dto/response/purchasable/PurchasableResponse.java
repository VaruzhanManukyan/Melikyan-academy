package com.melikyan.academy.dto.response.purchasable;

import com.melikyan.academy.entity.enums.PurchasableType;
import com.melikyan.academy.dto.response.category.CategoryShortResponse;

import java.util.UUID;
import java.time.OffsetDateTime;

public record PurchasableResponse(
        UUID id,
        String title,
        String description,
        PurchasableType type,
        CategoryShortResponse category,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
