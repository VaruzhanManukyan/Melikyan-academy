package com.melikyan.academy.dto.request.examSubmission;

import com.melikyan.academy.entity.enums.ExamStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateExamSubmissionRequest(
        String note,

        @NotNull(message = "examSubmission.status.notNull")
        ExamStatus status,

        @NotNull(message = "examSubmission.answerPayload.notNull")
        Map<String, Object> answerPayload,

        @NotNull(message = "examSubmission.userId.notNull")
        UUID userId,

        @NotNull(message = "examSubmission.taskId.notNull")
        UUID taskId
) {
}
