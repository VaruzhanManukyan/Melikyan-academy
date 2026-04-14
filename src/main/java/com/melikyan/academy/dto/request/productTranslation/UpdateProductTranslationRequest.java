package com.melikyan.academy.dto.request.productTranslation;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProductTranslationRequest(
        @Size(max = 255, message = "productTranslation.title.size")
        String title,

        String description,

        @Size(min = 2, max = 2, message = "productTranslation.code.size")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "productTranslation.code.invalid")
        String code
) {
}
