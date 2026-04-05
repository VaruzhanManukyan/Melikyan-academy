package com.melikyan.academy.dto.response.examSubmission;

import com.melikyan.academy.entity.enums.ExamStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record ExamSubmissionResponse(
        UUID id,
        String note,
        ExamStatus status,
        Map<String, Object> answerPayload,
        UUID userId,
        UUID taskId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
