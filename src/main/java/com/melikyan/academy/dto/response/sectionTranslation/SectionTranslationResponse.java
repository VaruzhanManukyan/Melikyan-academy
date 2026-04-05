package com.melikyan.academy.dto.response.sectionTranslation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SectionTranslationResponse(
        UUID id,
        String title,
        String description,
        String code,
        UUID examSectionId,
        UUID createdById,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

