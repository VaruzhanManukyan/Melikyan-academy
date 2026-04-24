package com.melikyan.academy.dto.request.homeworkSubmission;

import com.melikyan.academy.entity.enums.HomeworkStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateHomeworkSubmissionRequest(
        @NotNull(message = "Status is required")
        HomeworkStatus status,

        String note
) {
}
