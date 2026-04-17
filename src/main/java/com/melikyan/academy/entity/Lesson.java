package com.melikyan.academy.entity;

import com.melikyan.academy.entity.enums.LessonState;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.base.BaseEntity;
import com.melikyan.academy.entity.enums.LessonType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;
import java.time.Duration;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "lessons",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lesson_order_index_course",
                        columnNames = {"course_id", "order_index"}
                )
        }
)
public class Lesson extends BaseEntity {
    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private LessonType lessonType;

    @Column(name = "value_url", nullable = false)
    private String valueUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "state", nullable = false)
    private LessonState state;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Column(name = "duration", nullable = false, columnDefinition = "interval")
    private Duration duration;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "lesson")
    private List<LessonAttendance> lessonAttendances;

    @OneToMany(mappedBy = "lesson")
    private List<LessonTranslation> lessonTranslations;
}
