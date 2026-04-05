package com.melikyan.academy.dto.response.lessonAttendance;

import com.melikyan.academy.entity.enums.AttendanceStatus;

import java.util.UUID;
import java.time.OffsetDateTime;

public record LessonAttendanceResponse(
        UUID id,
        String note,
        AttendanceStatus status,
        UUID userId,
        UUID lessonId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
