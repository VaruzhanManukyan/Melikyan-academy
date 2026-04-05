package com.melikyan.academy.dto.response.product;

import com.melikyan.academy.dto.response.purchasable.PurchasableShortResponse;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductResponse(
        UUID id,
        String title,
        String description,
        BigDecimal price,
        boolean isPrivate,
        List<PurchasableShortResponse> purchasables,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
