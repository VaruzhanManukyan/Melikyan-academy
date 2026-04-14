package com.melikyan.academy.dto.request.productTranslation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateProductTranslationRequest(
        @NotBlank(message = "productTranslation.title.notBlank")
        @Size(max = 255, message = "productTranslation.title.size")
        String title,

        String description,

        @NotBlank(message = "productTranslation.code.notBlank")
        @Size(min = 2, max = 2, message = "productTranslation.code.size")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "productTranslation.code.invalid")
        String code,

        @NotNull(message = "productTranslation.productId.notNull")
        UUID productId,

        @NotNull(message = "productTranslation.createdById.notNull")
        UUID createdById
) {
}
