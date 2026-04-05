package com.melikyan.academy.dto.response.examTask;

import com.melikyan.academy.entity.enums.TaskType;

import java.util.Map;
import java.util.UUID;
import java.time.Duration;
import java.time.OffsetDateTime;

public record ExamTaskResponse(
        UUID id,
        Integer orderIndex,
        Integer point,
        TaskType type,
        Duration duration,
        Map<String, Object> contentPayload,
        UUID sectionId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
