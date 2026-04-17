package com.melikyan.academy.dto.response.lesson;

import com.melikyan.academy.entity.enums.LessonType;
import com.melikyan.academy.entity.enums.LessonState;

import java.util.UUID;
import java.time.Duration;
import java.time.OffsetDateTime;

public record LessonResponse(
        UUID id,
        Integer orderIndex,
        String title,
        String description,
        LessonType lessonType,
        String valueUrl,
        LessonState state,
        OffsetDateTime startsAt,
        Duration duration,
        UUID courseId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
