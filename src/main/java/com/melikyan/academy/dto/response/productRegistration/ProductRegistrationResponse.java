package com.melikyan.academy.dto.response.productRegister;

import com.melikyan.academy.entity.enums.RegistrationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductRegisterResponse(
        UUID id,
        UUID productId,
        UUID userId,
        UUID transactionId,
        RegistrationStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
