package com.melikyan.academy.dto.request.homework;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;
import java.time.OffsetDateTime;

public record UpdateHomeworkRequest(
        @Min(value = 1, message = "{homework.orderIndex.min}")
        Integer orderIndex,

        @Size(max = 50, message = "{homework.title.size}")
        String title,

        @Size(max = 500, message = "{homework.description.size}")
        String description,

        OffsetDateTime dueDate,

        @JsonProperty("is_published")
        Boolean isPublished
) {
}
