package com.melikyan.academy.dto.request.product;

import com.melikyan.academy.entity.enums.ProductType;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record CreateProductRequest(
        @Size(max = 50, message = "{product.title.size}")
        String title,

        @Size(max = 500, message = "{product.description.size}")
        String description,

        @NotNull(message = "{product.type.notNull}")
        ProductType type,

        @NotNull(message = "{product.price.notNull}")
        @Digits(integer = 10, fraction = 2, message = "{product.price.digits}")
        @DecimalMin(value = "0.00", inclusive = true, message = "{product.price.min}")
        BigDecimal price,

        @JsonProperty("is_private")
        @NotNull(message = "{product.isPrivate.notNull}")
        Boolean isPrivate,

        @NotEmpty(message = "{product.contentItemIds.notEmpty}")
        List<UUID> contentItemIds,

        @NotNull(message = "{product.categoryId.notNull}")
        UUID categoryId,

        @NotNull(message = "{product.createdById.notNull}")
        UUID createdById
) {
}

