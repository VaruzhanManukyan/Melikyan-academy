package com.melikyan.academy.dto.response.contentItem;

import com.melikyan.academy.entity.enums.ContentItemType;

import java.util.UUID;

public record ContentItemShortResponse(
        UUID id,
        String title,
        String description,
        ContentItemType type
) {
}
