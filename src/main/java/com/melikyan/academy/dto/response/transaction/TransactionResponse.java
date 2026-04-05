package com.melikyan.academy.dto.response.transaction;

import com.melikyan.academy.entity.enums.PaymentMethod;
import com.melikyan.academy.entity.enums.TransactionType;
import com.melikyan.academy.entity.enums.TransactionStatus;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        TransactionStatus status,
        TransactionType transactionType,
        UUID userId,
        UUID productId,
        OffsetDateTime createdAt
) {
}