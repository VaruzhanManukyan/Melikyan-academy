package com.melikyan.academy.dto.request.lessonAttendance;

import com.melikyan.academy.entity.enums.AttendanceStatus;

import java.util.UUID;

public record UpdateLessonAttendanceRequest(
        String note,
        AttendanceStatus status,
        UUID lessonId
) {
}
