package com.melikyan.academy.dto.request.productRegister;

import com.melikyan.academy.entity.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateProductRegisterRequest(
        @NotNull(message = "productRegister.productId.notNull")
        UUID productId,

        @NotNull(message = "productRegister.userId.notNull")
        UUID userId,

        UUID transactionId,

        @NotNull(message = "productRegister.status.notNull")
        RegistrationStatus status
) {
}
