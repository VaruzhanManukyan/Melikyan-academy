package com.melikyan.academy.dto.request.examSection;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.util.UUID;

public record CreateExamSectionRequest(
        @NotNull(message = "{examSection.orderIndex.notNull}")
        @Min(value = 1, message = "{examSection.orderIndex.min}")
        Integer orderIndex,

        @NotBlank(message = "{examSection.title.notBlank}")
        @Size(max = 255, message = "{examSection.title.size}")
        String title,

        String description,

        Duration duration,

        @NotNull(message = "{examSection.examId.notNull}")
        UUID examId,

        UUID createdById
) {
}