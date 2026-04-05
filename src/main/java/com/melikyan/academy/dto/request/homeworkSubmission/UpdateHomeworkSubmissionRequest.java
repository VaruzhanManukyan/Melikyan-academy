package com.melikyan.academy.dto.request.homeworkSubmission;

import com.melikyan.academy.entity.enums.HomeworkStatus;

import java.util.Map;
import java.util.UUID;

public record UpdateHomeworkSubmissionRequest(
        String note,
        HomeworkStatus status,
        Map<String, Object> answerPayload,
        UUID taskId
) {
}
