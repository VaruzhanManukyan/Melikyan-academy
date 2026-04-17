package com.melikyan.academy.dto.request.lesson;

import com.melikyan.academy.entity.enums.LessonState;
import com.melikyan.academy.entity.enums.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import java.time.Duration;
import java.time.OffsetDateTime;

public record UpdateLessonRequest(
        @Min(value = 1, message = "{lesson.orderIndex.min}")
        Integer orderIndex,

        @Size(max = 50, message = "{lesson.title.size}")
        String title,

        @Size(max = 500, message = "{lesson.description.size}")
        String description,

        LessonType lessonType,

        String valueUrl,

        LessonState state,

        OffsetDateTime startsAt,

        Duration duration,

        UUID courseId
) {
}
