package com.melikyan.academy.dto.response.productRegistration;

import com.melikyan.academy.entity.enums.RegistrationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductRegistrationResponse(
        UUID id,
        RegistrationStatus status,
        UUID productId,
        UUID userId,
        UUID transactionId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
