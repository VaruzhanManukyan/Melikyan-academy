package com.melikyan.academy.dto.request.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateCourseRequest(
        @NotNull(message = "{course.startDate.notNull}")
        OffsetDateTime startDate,

        @NotNull(message = "{course.durationWeeks.notNull}")
        @Min(value = 1, message = "{course.durationWeeks.min}")
        Integer durationWeeks,

        @NotNull(message = "{course.purchasableId.notNull}")
        UUID purchasableId
) {
}
