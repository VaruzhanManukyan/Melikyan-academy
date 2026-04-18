package com.melikyan.academy.dto.request.certificate;

import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

public record UpdateCertificateRequest(
        @Size(max = 255, message = "{certificate.certificateCode.size}")
        String certificateCode,

        OffsetDateTime issueDate,

        OffsetDateTime expiryDate,

        Map<String, Object> metadata,

        String pdfUrl,

        UUID contentItemId
) {
}
