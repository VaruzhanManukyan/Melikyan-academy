package com.melikyan.academy.dto.response.userProcess;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UserProcessResponse(
        UUID id,
        Integer currentStep,
        Integer totalSteps,
        BigDecimal scoreAccumulated,
        UUID userId,
        UUID purchasableId,
        OffsetDateTime lastAccessedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
