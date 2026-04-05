package com.melikyan.academy.dto.response.certificate;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record CertificateResponse(
        UUID id,
        String certificateCode,
        OffsetDateTime issueDate,
        OffsetDateTime expiryDate,
        Map<String, Object> metadata,
        String pdfUrl,
        UUID userId,
        UUID purchasableId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
