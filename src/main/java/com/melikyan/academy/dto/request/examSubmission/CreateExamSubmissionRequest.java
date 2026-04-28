package com.melikyan.academy.dto.request.examSubmission;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateExamSubmissionRequest(
        @NotNull(message = "{examSubmission.answerPayload.notNull}")
        Map<String, Object> answerPayload,

        @NotNull(message = "{examSubmission.taskId.notNull}")
        UUID taskId
) {
}
