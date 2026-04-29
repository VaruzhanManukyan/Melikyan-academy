package com.melikyan.academy.dto.response.lessonTranslation;

import java.util.UUID;
import java.time.OffsetDateTime;

public record LessonTranslationResponse(
        UUID id,
        String code,
        String title,
        String description,
        String valueUrl,
        UUID lessonId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
