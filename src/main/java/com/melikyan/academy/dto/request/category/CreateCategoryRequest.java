package com.melikyan.academy.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank(message = "{category.title.notBlank}")
        @Size(max = 50, message = "{category.title.size}")
        String title,

        String description,

        @NotNull(message = "{category.createdById.notNull}")
        UUID createdById
) {
}
