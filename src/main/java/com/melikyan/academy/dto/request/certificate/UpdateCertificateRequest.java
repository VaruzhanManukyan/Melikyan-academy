package com.melikyan.academy.dto.request.certificate;

import java.time.OffsetDateTime;
import java.util.Map;

public record UpdateCertificateRequest(
        OffsetDateTime expiryDate,
        Map<String, Object> metadata
) {
}