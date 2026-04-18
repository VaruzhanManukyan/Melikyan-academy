package com.melikyan.academy.dto.request.exam;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateExamRequest(
        @NotNull(message = "{exam.contentItemId.notNull}")
        UUID contentItemId
) {
}
