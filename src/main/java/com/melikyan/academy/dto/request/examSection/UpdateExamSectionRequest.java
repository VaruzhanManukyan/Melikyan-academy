package com.melikyan.academy.dto.request.examSection;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.util.UUID;

public record UpdateExamSectionRequest(
        @Min(value = 1, message = "examSection.orderIndex.min")
        Integer orderIndex,

        @Size(max = 255, message = "examSection.title.size")
        String title,

        String description,

        Duration duration,

        UUID examId
) {
}