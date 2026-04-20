package com.melikyan.academy.dto.request.product;

import com.melikyan.academy.entity.enums.ProductType;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @Size(max = 50, message = "{product.title.size}")
        String title,

        @Size(max = 500, message = "{product.description.size}")
        String description,

        ProductType type,

        @Digits(integer = 10, fraction = 2, message = "{product.price.digits}")
        @DecimalMin(value = "0.00", inclusive = true, message = "{product.price.min}")
        BigDecimal price,

        @JsonProperty("is_private")
        Boolean isPrivate,

        UUID categoryId,

        List<UUID> contentItemIds
) {
}
