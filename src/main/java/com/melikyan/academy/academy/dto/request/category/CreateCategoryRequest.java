package com.melikyan.academy.academy.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank(message = "Category title must not be blank.")
        @Size(max = 50, message = "Category title must not exceed 50 characters.")
        String title,

        String description,

        @NotNull(message = "Created by user ID must not be null.")
        UUID createdById
) {
}
