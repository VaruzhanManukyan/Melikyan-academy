package com.melikyan.academy.dto.response.lesson;

import com.melikyan.academy.entity.enums.SessionType;
import com.melikyan.academy.entity.enums.SessionState;

import java.util.UUID;
import java.time.Duration;
import java.time.OffsetDateTime;

public record LessonResponse(
        UUID id,
        Integer orderIndex,
        String title,
        String description,
        SessionType sessionType,
        String valueUrl,
        SessionState state,
        OffsetDateTime startsAt,
        Duration duration,
        UUID courseId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
