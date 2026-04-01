package com.melikyan.academy.entity;

import lombok.AccessLevel;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.enums.ExamStatus;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.Map;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "exam_submissions",
        indexes = {
                @Index(name = "idx_exam_submissions_user_id", columnList = "user_id"),
                @Index(name = "idx_exam_submissions_task_id", columnList = "task_id")
        }
)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
public class ExamSubmission extends BaseEntitySoftDelete {
    @Column(name = "node")
    private String node;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExamStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer_payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> answerPayload;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private ExamTask task;
}
