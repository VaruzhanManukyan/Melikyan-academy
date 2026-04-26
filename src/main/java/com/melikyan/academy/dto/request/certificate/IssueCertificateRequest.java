package com.melikyan.academy.dto.request.certificate;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

public record IssueCertificateRequest(
        @NotNull(message = "{certificate.userId.notNull}")
        UUID userId,

        @NotNull(message = "{certificate.contentItemId.notNull}")
        UUID contentItemId,

        OffsetDateTime expiryDate,

        Map<String, Object> metadata
) {
}