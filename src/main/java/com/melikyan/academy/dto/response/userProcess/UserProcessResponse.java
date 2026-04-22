package com.melikyan.academy.dto.response.userProcess;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UserProcessResponse(
        UUID id,
        Integer currentStep,
        BigDecimal scoreAccumulated,
        UUID userId,
        UUID contentItemId,
        OffsetDateTime lastAccessedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
