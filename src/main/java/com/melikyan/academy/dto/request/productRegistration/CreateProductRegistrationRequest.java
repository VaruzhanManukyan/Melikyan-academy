package com.melikyan.academy.dto.request.productRegistration;

import com.melikyan.academy.entity.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProductRegistrationRequest(
        @NotNull(message = "{productRegister.productId.notNull}")
        UUID productId,

        @NotNull(message = "{productRegister.userId.notNull}")
        UUID userId,

        UUID transactionId
) {
}
