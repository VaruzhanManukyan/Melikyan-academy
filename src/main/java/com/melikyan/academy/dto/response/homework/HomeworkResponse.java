package com.melikyan.academy.dto.response.homework;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HomeworkResponse(
        UUID id,
        Integer orderIndex,
        String title,
        String description,
        OffsetDateTime dueDate,
        boolean isPublished,
        UUID lessonId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
