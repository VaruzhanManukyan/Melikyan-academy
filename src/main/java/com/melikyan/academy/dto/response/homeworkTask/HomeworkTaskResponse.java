package com.melikyan.academy.dto.response.homeworkTask;

import com.melikyan.academy.entity.enums.TaskType;

import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

public record HomeworkTaskResponse(
        UUID id,
        Integer orderIndex,
        Integer point,
        TaskType type,
        Map<String, Object> payloadContent,
        UUID homeworkId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}