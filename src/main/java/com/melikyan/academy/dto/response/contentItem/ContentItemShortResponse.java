package com.melikyan.academy.dto.response.contentItem;

import com.melikyan.academy.entity.enums.PurchasableType;

import java.util.UUID;

public record PurchasableShortResponse(
        UUID id,
        String title,
        String description,
        PurchasableType type
) {
}
