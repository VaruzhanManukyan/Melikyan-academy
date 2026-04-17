package com.melikyan.academy.dto.response.product;

import com.melikyan.academy.dto.response.category.CategoryShortResponse;
import com.melikyan.academy.dto.response.contentItem.ContentItemShortResponse;

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
        List<ContentItemShortResponse> contetItems,
        CategoryShortResponse category,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
