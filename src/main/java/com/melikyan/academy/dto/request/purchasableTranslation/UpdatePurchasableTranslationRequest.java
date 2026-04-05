package com.melikyan.academy.dto.request.purchasableTranslation;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UpdatePurchasableTranslationRequest(
        @Size(max = 255, message = "{purchasableTranslation.title.size}")
        String title,

        String description,

        @Size(min = 2, max = 2, message = "{purchasableTranslation.code.size}")
        @Pattern(regexp = "^[a-zA-Z]{2}$", message = "{purchasableTranslation.code.invalid}")
        String code
) {
}
