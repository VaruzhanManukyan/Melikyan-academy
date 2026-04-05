package com.melikyan.academy.dto.request.purchasable;

import com.melikyan.academy.entity.enums.PurchasableType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreatePurchasableRequest(
        @NotBlank(message = "purchasable.title.notBlank")
        @Size(max = 50, message = "{purchasable.title.size}")
        String title,

        String description,

        @NotEmpty(message = "{purchasable.type.notBlank}")
        PurchasableType type,

        @NotNull(message = "{purchasable.categoryId.notNull}")
        UUID categoryId,

        @NotNull(message = "{purchasable.createdById.notNull}")
        UUID createdById
) {
}
