package com.melikyan.academy.dto.request.exam;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateExamRequest(
        @NotBlank(message = "{exam.title.notBlank}")
        @Size(max = 50, message = "{exam.title.size}")
        String title,

        @Size(max = 500, message = "{exam.description.size}")
        String description,

        @NotNull(message = "{exam.createdById.notNull}")
        UUID createdById
) {
}
