package com.melikyan.academy.dto.request.course;

import jakarta.validation.constraints.Min;

import java.time.OffsetDateTime;

public record UpdateCourseRequest(
        OffsetDateTime startDate,

        @Min(value = 1, message = "{course.durationWeeks.min}")
        Integer durationWeeks
) {
}
