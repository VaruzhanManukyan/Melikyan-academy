package com.melikyan.academy.dto.request.lessonTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateLessonTranslationRequest(
        @Size(max = 255, message = "lessonTranslation.title.size")
        String title,

        String description,

        @Size(min = 2, max = 2, message = "lessonTranslation.code.size")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "lessonTranslation.code.invalid")
        String code
) {
}
