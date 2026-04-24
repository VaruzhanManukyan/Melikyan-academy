package com.melikyan.academy.dto.request.homeworkSubmission;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateHomeworkSubmissionRequest(
        @NotNull(message = "{homeworkSubmission.answerPayload.notNull}")
        Map<String, Object> answerPayload,

        @NotNull(message = "{homeworkSubmission.taskId.notNull}")
        UUID taskId
) {
}