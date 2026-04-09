package com.melikyan.academy.entity;

import jakarta.persistence.*;
import com.melikyan.academy.entity.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(
        name = "lesson_translations",
        indexes = {
                @Index(name = "uk_lesson_translation_lesson_id", columnList = "lesson_id")
        }
)
public class LessonTranslation extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(length = 2, name = "code",  nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}