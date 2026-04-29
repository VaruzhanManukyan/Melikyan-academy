package com.melikyan.academy.dto.request.lessonTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateLessonTranslationRequest(
        @NotBlank(message = "{lessonTranslation.title.notBlank}")
        @Size(max = 255, message = "{lessonTranslation.title.size}")
        String title,

        @Size(max = 500, message = "{lessonTranslation.description.size}")
        String description,

        @NotBlank(message = "{lessonTranslation.valueUrl.notBlank}")
        @Size(max = 255, message = "{lessonTranslation.valueUrl.size}")
        String valueUrl,

        @NotBlank(message = "{lessonTranslation.code.notBlank}")
        @Size(min = 2, max = 2, message = "{lessonTranslation.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{lessonTranslation.code.invalid}")
        String code,

        @NotNull(message = "{lessonTranslation.lessonId.notNull}")
        UUID lessonId,

        @NotNull(message = "{lessonTranslation.createdById.notNull}")
        UUID createdById
) {
}
