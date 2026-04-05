package com.melikyan.academy.dto.request.professor;

import java.util.UUID;

public record UpdateProfessorRequest(
        UUID userId,
        UUID courseId
) {
}
