package com.melikyan.academy.dto.response.exam;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExamResponse(
        UUID id,
        UUID purchasableId,
        OffsetDateTime updatedAt
) {
}
