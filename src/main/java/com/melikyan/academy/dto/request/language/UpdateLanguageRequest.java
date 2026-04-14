package com.melikyan.academy.dto.request.language;

import jakarta.validation.constraints.Size;

public record UpdateLanguageRequest(
        @Size(max = 50, message = "language.name.size")
        String name
) {
}
