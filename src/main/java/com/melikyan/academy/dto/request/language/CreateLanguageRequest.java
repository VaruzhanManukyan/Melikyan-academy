package com.melikyan.academy.dto.request.language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateLanguageRequest(
        @NotBlank(message = "{language.code.notBlank}")
        @Size(max = 2, message = "{language.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{language.code.invalid}")
        String code,

        @NotBlank(message = "{language.name.notBlank}")
        @Size(max = 50, message = "{language.name.size}")
        String name,

        @NotNull(message = "{language.createdById.notNull}")
        UUID createdById
) {
}