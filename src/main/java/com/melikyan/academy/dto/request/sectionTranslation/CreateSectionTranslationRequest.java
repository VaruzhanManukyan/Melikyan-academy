package com.melikyan.academy.dto.request.sectionTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateSectionTranslationRequest(
        @NotBlank(message = "sectionTranslation.title.notBlank")
        @Size(max = 255, message = "sectionTranslation.title.size")
        String title,

        String description,

        @NotBlank(message = "sectionTranslation.code.notBlank")
        @Size(min = 2, max = 2, message = "sectionTranslation.code.size")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "sectionTranslation.code.invalid")
        String code,

        @NotNull(message = "sectionTranslation.examSectionId.notNull")
        UUID examSectionId,

        @NotNull(message = "sectionTranslation.createdById.notNull")
        UUID createdById
) {
}
