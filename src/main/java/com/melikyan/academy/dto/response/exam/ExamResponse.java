package com.melikyan.academy.dto.response.exam;

import com.melikyan.academy.entity.enums.ContentItemType;

import java.util.UUID;
import java.time.OffsetDateTime;

public record ExamResponse(
        UUID id,
        String title,
        String description,
        ContentItemType type,
        UUID createdById,
        UUID contentItemId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
