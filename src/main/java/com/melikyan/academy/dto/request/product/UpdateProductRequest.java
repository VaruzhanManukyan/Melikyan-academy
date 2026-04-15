package com.melikyan.academy.dto.request.product;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record UpdateProductRequest(
        @Digits(integer = 10, fraction = 2, message = "{product.price.digits}")
        @DecimalMin(value = "0.00", inclusive = true, message = "{product.price.min}")
        BigDecimal price,

        @JsonProperty("is_private")
        Boolean isPrivate,

        List<UUID> purchasableIds
) {
}
