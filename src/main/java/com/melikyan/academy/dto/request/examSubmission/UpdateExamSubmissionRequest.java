package com.melikyan.academy.dto.request.examSubmission;

import com.melikyan.academy.entity.enums.ExamStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateExamSubmissionRequest(
        @NotNull(message = "Status is required")
        ExamStatus status,

        String note
) {
}
