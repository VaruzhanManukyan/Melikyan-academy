package com.melikyan.academy.dto.request.purchasable;

import com.melikyan.academy.entity.enums.PurchasableType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdatePurchasableRequest(
        @NotBlank(message = "purchasable.title.notBlank")
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
