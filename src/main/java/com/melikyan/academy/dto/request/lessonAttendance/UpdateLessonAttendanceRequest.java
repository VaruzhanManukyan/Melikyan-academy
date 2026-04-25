package com.melikyan.academy.dto.request.lessonAttendance;

import com.melikyan.academy.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLessonAttendanceRequest(
        String note,

        @NotNull(message = "{lessonAttendance.status.notNull}")
        AttendanceStatus status
) {
}
