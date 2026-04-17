package com.melikyan.academy.dto.response.course;

import com.melikyan.academy.entity.enums.ContentItemType;

import java.util.UUID;
import java.time.OffsetDateTime;

public record CourseResponse(
        UUID id,
        String title,
        String description,
        ContentItemType type,
        Integer durationWeeks,
        OffsetDateTime startDate,
        UUID createdById,
        UUID contentItemId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
