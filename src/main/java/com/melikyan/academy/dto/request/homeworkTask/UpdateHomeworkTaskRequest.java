package com.melikyan.academy.dto.request.homeworkTask;

import com.melikyan.academy.entity.enums.TaskType;
import jakarta.validation.constraints.Min;

import java.util.Map;
import java.util.UUID;

public record UpdateHomeworkTaskRequest(
        @Min(value = 1, message = "{homeworkTask.orderIndex.min}")
        Integer orderIndex,

        @Min(value = 1, message = "{homeworkTask.point.min}")
        Integer point,

        TaskType type,

        Map<String, Object> contentPayload
) {
}
