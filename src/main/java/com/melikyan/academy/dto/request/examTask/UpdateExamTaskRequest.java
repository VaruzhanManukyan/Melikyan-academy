package com.melikyan.academy.dto.request.examTask;

import com.melikyan.academy.entity.enums.TaskType;
import jakarta.validation.constraints.Min;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public record UpdateExamTaskRequest(
        @Min(value = 1, message = "examTask.orderIndex.min")
        Integer orderIndex,

        @Min(value = 1, message = "examTask.point.min")
        Integer point,

        TaskType type,

        Duration duration,

        Map<String, Object> contentPayload,

        UUID sectionId
) {
}
