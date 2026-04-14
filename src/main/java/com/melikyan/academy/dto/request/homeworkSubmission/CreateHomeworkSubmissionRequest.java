package com.melikyan.academy.dto.request.homeworkSubmission;

import com.melikyan.academy.entity.enums.HomeworkStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateHomeworkSubmissionRequest(
        String note,

        @NotNull(message = "homeworkSubmission.status.notNull")
        HomeworkStatus status,

        @NotNull(message = "homeworkSubmission.answerPayload.notNull")
        Map<String, Object> answerPayload,

        @NotNull(message = "homeworkSubmission.userId.notNull")
        UUID userId,

        @NotNull(message = "homeworkSubmission.taskId.notNull")
        UUID taskId
) {
}