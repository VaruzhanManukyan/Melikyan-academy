package com.melikyan.academy.dto.request.language;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateLanguageRequest(
        @Size(max = 50, message = "{language.name.size}")
        String name,

        @Size(max = 2, message = "{language.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{language.code.invalid}")
        String code
) {
}
