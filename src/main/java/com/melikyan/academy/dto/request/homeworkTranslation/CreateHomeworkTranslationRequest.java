package com.melikyan.academy.dto.request.homeworkTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateHomeworkTranslationRequest(
        @NotBlank(message = "homeworkTranslation.title.notBlank")
        @Size(max = 255, message = "homeworkTranslation.title.size")
        String title,

        String description,

        @NotBlank(message = "homeworkTranslation.code.notBlank")
        @Size(min = 2, max = 2, message = "homeworkTranslation.code.size")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "homeworkTranslation.code.invalid")
        String code,

        @NotNull(message = "homeworkTranslation.homeworkId.notNull")
        UUID homeworkId,

        @NotNull(message = "homeworkTranslation.createdById.notNull")
        UUID createdById
) {
}
