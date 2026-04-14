package com.melikyan.academy.dto.request.lesson;

import com.melikyan.academy.entity.enums.SessionType;
import com.melikyan.academy.entity.enums.SessionState;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;
import java.time.Duration;
import java.time.OffsetDateTime;

public record CreateLessonRequest(
        @NotNull(message = "lesson.orderIndex.notNull")
        @Min(value = 1, message = "lesson.orderIndex.min")
        Integer orderIndex,

        @NotBlank(message = "lesson.title.notBlank")
        @Size(max = 50, message = "lesson.title.size")
        String title,

        String description,

        @NotNull(message = "lesson.sessionType.notNull")
        SessionType sessionType,

        @NotBlank(message = "lesson.valueUrl.notBlank")
        String valueUrl,

        @NotNull(message = "lesson.state.notNull")
        SessionState state,

        @NotNull(message = "lesson.startsAt.notNull")
        OffsetDateTime startsAt,

        @NotNull(message = "lesson.duration.notNull")
        Duration duration,

        @NotNull(message = "lesson.courseId.notNull")
        UUID courseId,

        @NotNull(message = "lesson.createdById.notNull")
        UUID createdById
) {
}
