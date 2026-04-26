package com.melikyan.academy.dto.request.certificate;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

public record CreateCertificateRequest(
        @NotBlank(message = "{certificate.certificateCode.notBlank}")
        @Size(max = 255, message = "{certificate.certificateCode.size}")
        String certificateCode,

        @NotNull(message = "{certificate.issueDate.notNull}")
        OffsetDateTime issueDate,

        OffsetDateTime expiryDate,

        @NotNull(message = "{certificate.metadata.notNull}")
        Map<String, Object> metadata,

        String pdfUrl,

        @NotNull(message = "{certificate.userId.notNull}")
        UUID userId,

        @NotNull(message = "{certificate.contentItemId.notNull}")
        UUID contentItemId
) {
}