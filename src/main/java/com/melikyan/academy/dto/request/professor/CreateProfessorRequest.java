package com.melikyan.academy.dto.request.professor;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProfessorRequest(
        @NotNull(message = "{professor.userId.notNull}")
        UUID userId,

        @NotNull(message = "{professor.courseId.notNull}")
        UUID courseId
) {
}
