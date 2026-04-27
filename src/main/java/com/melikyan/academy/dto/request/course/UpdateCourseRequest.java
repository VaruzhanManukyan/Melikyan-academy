package com.melikyan.academy.dto.request.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record UpdateCourseRequest(
        @Size(max = 50, message = "{course.title.size}")
        String title,

        @Size(max = 500, message = "{course.description.size}")
        String description,

        OffsetDateTime startDate,

        @Min(value = 1, message = "{course.durationWeeks.min}")
        Integer durationWeeks
) {
}
