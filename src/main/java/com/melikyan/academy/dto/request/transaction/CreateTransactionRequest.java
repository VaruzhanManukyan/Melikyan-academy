package com.melikyan.academy.dto.request.transaction;

import com.melikyan.academy.entity.enums.PaymentMethod;
import com.melikyan.academy.entity.enums.TransactionType;
import com.melikyan.academy.entity.enums.TransactionStatus;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;

import java.util.UUID;
import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull(message = "{transaction.amount.notNull}")
        @DecimalMin(value = "0.00", inclusive = false, message = "{transaction.amount.min}")
        @Digits(integer = 10, fraction = 2, message = "{transaction.amount.digits}")
        BigDecimal amount,

        @NotBlank(message = "{transaction.currency.notBlank}")
        @Size(min = 3, max = 3, message = "{transaction.currency.size}")
        @Pattern(regexp = "^[A-Z]{3}$", message = "{transaction.currency.invalid}")
        String currency,

        @NotNull(message = "{transaction.paymentMethod.notNull}")
        PaymentMethod paymentMethod,

        @NotNull(message = "{transaction.status.notNull}")
        TransactionStatus status,

        @NotNull(message = "{transaction.transactionType.notNull}")
        TransactionType transactionType,

        @NotNull(message = "{transaction.userId.notNull}")
        UUID userId,

        @NotNull(message = "{transaction.productId.notNull}")
        UUID productId
) {
}