package com.melikyan.academy.dto.response.homeworkSubmission;

import com.melikyan.academy.entity.enums.HomeworkStatus;

import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

public record HomeworkSubmissionResponse(
        UUID id,
        String note,
        HomeworkStatus status,
        Map<String, Object> answerPayload,
        UUID userId,
        UUID taskId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
