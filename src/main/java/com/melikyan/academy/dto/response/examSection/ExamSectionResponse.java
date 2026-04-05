package com.melikyan.academy.dto.response.examSection;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExamSectionResponse(
        UUID id,
        Integer orderIndex,
        String title,
        String description,
        Duration duration,
        UUID examId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
