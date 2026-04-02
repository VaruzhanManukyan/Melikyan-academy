package com.melikyan.academy.entity;

import lombok.AccessLevel;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.enums.AttendanceStatus;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "lesson_attendances",
        indexes = {
                @Index(name = "idx_lesson_attendance_user_id", columnList = "user_id"),
                @Index(name = "idx_lesson_attendance_lesson_id", columnList = "lesson_id")
        }
)
public class LessonAttendance extends BaseEntitySoftDelete {
    @Column(name = "node")
    private String node;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;
}