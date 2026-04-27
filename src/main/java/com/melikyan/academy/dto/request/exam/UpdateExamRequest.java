package com.melikyan.academy.dto.request.exam;

import jakarta.validation.constraints.Size;

public record UpdateExamRequest(
        @Size(max = 50, message = "{course.title.size}")
        String title,

        @Size(max = 500, message = "{course.description.size}")
        String description
) {
}
