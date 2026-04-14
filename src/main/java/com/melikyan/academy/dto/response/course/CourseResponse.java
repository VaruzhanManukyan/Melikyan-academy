package com.melikyan.academy.dto.response.course;

import com.melikyan.academy.entity.enums.PurchasableType;

import java.util.UUID;
import java.time.OffsetDateTime;

public record CourseResponse(
        UUID id,
        String title,
        String description,
        PurchasableType type,
        Integer durationWeeks,
        OffsetDateTime startDate,
        UUID categoryId,
        UUID createdById,
        UUID purchasableId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
