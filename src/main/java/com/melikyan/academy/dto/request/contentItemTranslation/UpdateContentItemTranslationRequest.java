package com.melikyan.academy.dto.request.contentItemTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateContentItemTranslationRequest(
        @Size(max = 255, message = "{contentItemTranslation.title.size}")
        String title,

        String description,

        @Size(min = 2, max = 2, message = "{contentItemTranslation.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{contentItemTranslation.code.invalid}")
        String code
) {
}
