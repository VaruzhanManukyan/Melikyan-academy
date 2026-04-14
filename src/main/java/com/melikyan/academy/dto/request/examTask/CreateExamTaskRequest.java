package com.melikyan.academy.dto.request.examTask;

import com.melikyan.academy.entity.enums.TaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;
import java.time.Duration;

public record CreateExamTaskRequest(
        @NotNull(message = "examTask.orderIndex.notNull")
        @Min(value = 1, message = "examTask.orderIndex.min")
        Integer orderIndex,

        @NotNull(message = "examTask.point.notNull")
        @Min(value = 1, message = "examTask.point.min")
        Integer point,

        @NotNull(message = "examTask.type.notNull")
        TaskType type,

        Duration duration,

        @NotNull(message = "examTask.contentPayload.notNull")
        Map<String, Object> contentPayload,

        @NotNull(message = "examTask.sectionId.notNull")
        UUID sectionId,

        @NotNull(message = "examTask.createdById.notNull")
        UUID createdById
) {
}
