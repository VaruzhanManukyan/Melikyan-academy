package com.melikyan.academy.dto.request.examSubmission;

import com.melikyan.academy.entity.enums.ExamStatus;

import java.util.Map;
import java.util.UUID;

public record UpdateExamSubmissionRequest(
        String note,
        ExamStatus status,
        Map<String, Object> answerPayload,
        UUID taskId
) {
}
