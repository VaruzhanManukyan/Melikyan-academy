package com.melikyan.academy.dto.request.lessonAttendance;

import com.melikyan.academy.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLessonAttendanceRequest(
        String note,

        @NotNull(message = "lessonAttendance.status.notNull")
        AttendanceStatus status,

        @NotNull(message = "lessonAttendance.userId.notNull")
        UUID userId,

        @NotNull(message = "lessonAttendance.lessonId.notNull")
        UUID lessonId
) {
}