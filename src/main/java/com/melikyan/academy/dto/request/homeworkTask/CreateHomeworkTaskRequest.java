package com.melikyan.academy.dto.request.homeworkTask;

import com.melikyan.academy.entity.enums.TaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateHomeworkTaskRequest(
        @NotNull(message = "{homeworkTask.orderIndex.notNull}")
        @Min(value = 1, message = "{homeworkTask.orderIndex.min}")
        Integer orderIndex,

        @NotNull(message = "{homeworkTask.point.notNull}")
        @Min(value = 1, message = "{homeworkTask.point.min}")
        Integer point,

        @NotNull(message = "{homeworkTask.type.notNull}")
        TaskType type,

        @NotNull(message = "{homeworkTask.contentPayload.notNull}")
        Map<String, Object> contentPayload,

        @NotNull(message = "{homeworkTask.homeworkId.notNull}")
        UUID lessonId,

        @NotNull(message = "{homeworkTask.createdById.notNull}")
        UUID createdById
) {
}
