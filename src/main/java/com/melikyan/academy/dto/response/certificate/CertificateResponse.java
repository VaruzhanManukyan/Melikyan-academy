package com.melikyan.academy.dto.response.certificate;

import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

public record CertificateResponse(
        UUID id,
        String certificateCode,
        OffsetDateTime issueDate,
        OffsetDateTime expiryDate,
        Map<String, Object> metadata,
        String pdfUrl,
        UUID userId,
        UUID contentItemId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
