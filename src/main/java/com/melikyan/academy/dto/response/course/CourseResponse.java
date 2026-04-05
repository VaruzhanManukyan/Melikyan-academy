package com.melikyan.academy.dto.response.course;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        OffsetDateTime startDate,
        Integer durationWeeks,
        UUID purchasableId,
        OffsetDateTime updatedAt
) {
}
