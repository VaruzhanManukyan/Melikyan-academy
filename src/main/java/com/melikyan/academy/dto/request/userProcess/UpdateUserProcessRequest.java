package com.melikyan.academy.dto.request.userProcess;

import jakarta.validation.constraints.Min;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UpdateUserProcessRequest(
        @Min(value = 0, message = "{userProcess.currentStep.min}")
        Integer currentStep,

        @Min(value = 1, message = "{userProcess.totalSteps.min}")
        Integer totalSteps,

        BigDecimal scoreAccumulated,

        UUID contentItemId,

        OffsetDateTime lastAccessedAt
) {
}
