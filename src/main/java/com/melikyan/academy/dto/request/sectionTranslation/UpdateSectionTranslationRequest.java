package com.melikyan.academy.dto.request.sectionTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdateSectionTranslationRequest(
        @Size(max = 255, message = "{sectionTranslation.title.size}")
        String title,

        String description,

        @Size(min = 2, max = 2, message = "{sectionTranslation.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{sectionTranslation.code.invalid}")
        String code
) {
}
