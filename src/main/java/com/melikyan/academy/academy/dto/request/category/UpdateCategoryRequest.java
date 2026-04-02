package com.melikyan.academy.academy.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank(message = "Category title must not be blank.")
        @Size(max = 50, message = "Category title must not exceed 50 characters.")
        String title,

        String description
) {
}
