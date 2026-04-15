package com.melikyan.academy.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(max = 50, message = "{category.title.size}")
        String title,

        String description
) {
}
