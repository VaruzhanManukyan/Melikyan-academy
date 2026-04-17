package com.melikyan.academy.dto.response.contentItem;

import com.melikyan.academy.entity.enums.ContentItemType;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ContentItemResponse(
        UUID id,
        String title,
        String description,
        ContentItemType type,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
