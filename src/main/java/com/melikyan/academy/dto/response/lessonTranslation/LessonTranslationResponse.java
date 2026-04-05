package com.melikyan.academy.dto.response.lessonTranslation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LessonTranslationResponse(
        UUID id,
        String title,
        String description,
        String code,
        UUID lessonId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
