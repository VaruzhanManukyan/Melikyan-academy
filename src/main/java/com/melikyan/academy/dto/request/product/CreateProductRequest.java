package com.melikyan.academy.dto.request.product;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotNull(message = "{product.price.notNull}")
        @Digits(integer = 10, fraction = 2, message = "{product.price.digits}")
        @DecimalMin(value = "0.00", inclusive = true, message = "{product.price.min}")
        BigDecimal price,

        @JsonProperty("is_private")
        @NotNull(message = "{product.isPrivate.notNull}")
        Boolean isPrivate,

        @NotEmpty(message = "{product.purchasableIds.notEmpty}")
        List<UUID> purchasableIds
) {
}

