package com.melikyan.academy.dto.request.course;

import com.melikyan.academy.entity.enums.ContentItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;
import java.time.OffsetDateTime;

public record CreateCourseRequest(
        @NotBlank(message = "{course.title.notBlank}")
        @Size(max = 50, message = "{course.title.size}")
        String title,

        @Size(max = 500, message = "{course.description.size}")
        String description,

        @NotNull(message = "{course.type.notNull}")
        ContentItemType type,

        @NotNull(message = "{course.startDate.notNull}")
        OffsetDateTime startDate,

        @NotNull(message = "{course.durationWeeks.notNull}")
        @Min(value = 1, message = "{course.durationWeeks.min}")
        Integer durationWeeks,

        @NotNull(message = "{course.createdById.notNull}")
        UUID createdById
) {
}
