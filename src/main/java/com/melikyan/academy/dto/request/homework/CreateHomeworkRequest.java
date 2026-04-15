package com.melikyan.academy.dto.request.homework;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;
import java.time.OffsetDateTime;

public record CreateHomeworkRequest(
        @NotNull(message = "{homework.orderIndex.notNull}")
        @Min(value = 1, message = "{homework.orderIndex.min}")
        Integer orderIndex,

        @NotBlank(message = "{homework.title.notBlank}")
        @Size(max = 50, message = "{homework.title.size}")
        String title,

        String description,

        @NotNull(message = "{homework.dueDate.notNull}")
        OffsetDateTime dueDate,

        @JsonProperty("is_published")
        @NotNull(message = "{homework.isPublished.notNull}")
        Boolean isPublished,

        @NotNull(message = "{homework.lessonId.notNull}")
        UUID lessonId,

        @NotNull(message = "{homework.createdById.notNull}")
        UUID createdById
) {
}
