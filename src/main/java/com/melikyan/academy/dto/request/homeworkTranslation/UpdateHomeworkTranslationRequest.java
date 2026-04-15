package com.melikyan.academy.dto.request.homeworkTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateHomeworkTranslationRequest(
        @Size(max = 255, message = "{homeworkTranslation.title.size}")
        String title,

        String description,

        @Size(min = 2, max = 2, message = "{homeworkTranslation.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{homeworkTranslation.code.invalid}")
        String code
) {
}
