package com.melikyan.academy.dto.request.userProcess;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateUserProcessRequest(
        @NotNull(message = "userProcess.currentStep.notNull")
        @Min(value = 0, message = "userProcess.currentStep.min")
        Integer currentStep,

        @NotNull(message = "userProcess.totalSteps.notNull")
        @Min(value = 1, message = "userProcess.totalSteps.min")
        Integer totalSteps,

        BigDecimal scoreAccumulated,

        @NotNull(message = "userProcess.userId.notNull")
        UUID userId,

        @NotNull(message = "userProcess.purchasableId.notNull")
        UUID purchasableId,

        OffsetDateTime lastAccessedAt
) {
}
