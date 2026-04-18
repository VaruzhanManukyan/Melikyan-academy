package com.melikyan.academy.dto.request.contentItemTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateContentItemTranslationRequest(
        @NotBlank(message = "{contentItemTranslation.title.notBlank}")
        @Size(max = 255, message = "{contentItemTranslation.title.size}")
        String title,

        String description,

        @NotBlank(message = "{contentItemTranslation.code.notBlank}")
        @Size(min = 2, max = 2, message = "{contentItemTranslation.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{contentItemTranslation.code.invalid}")
        String code,

        @NotNull(message = "{contentItemTranslation.contentItemId.notNull}")
        UUID contentItemId,

        @NotNull(message = "{contentItemTranslation.createdById.notNull}")
        UUID createdById
) {
}
