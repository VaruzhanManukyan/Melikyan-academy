package com.melikyan.academy.dto.request.contentItemTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateContentItemTranslationRequest(
        @NotBlank(message = "{purchasableTranslation.title.notBlank}")
        @Size(max = 255, message = "{purchasableTranslation.title.size}")
        String title,

        String description,

        @NotBlank(message = "{purchasableTranslation.code.notBlank}")
        @Size(min = 2, max = 2, message = "{purchasableTranslation.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{purchasableTranslation.code.invalid}")
        String code,

        @NotNull(message = "{purchasableTranslation.purchasableId.notNull}")
        UUID contentItemId,

        @NotNull(message = "{purchasableTranslation.createdById.notNull}")
        UUID createdById
) {
}
