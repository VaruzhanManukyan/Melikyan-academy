package com.melikyan.academy.dto.response.homeworkTranslation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HomeworkTranslationResponse(
        UUID id,
        String title,
        String description,
        String code,
        UUID homeworkId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
