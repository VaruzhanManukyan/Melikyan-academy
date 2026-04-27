package com.melikyan.academy.entity;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.enums.TaskType;
import com.melikyan.academy.entity.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.Map;
import java.util.List;
import java.time.Duration;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "exam_tasks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_task_order_index_section",
                        columnNames = {"section_id", "order_index"}
                )
        }
)
public class ExamTask extends BaseEntity {
    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "point", nullable = false)
    private int point;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "TASK_TYPE", nullable = false)
    private TaskType type;

    @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
    @Column(name = "duration", columnDefinition = "Interval")
    private Duration duration;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_payload", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> contentPayload;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private ExamSection section;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "task")
    private List<ExamSubmission> examSubmissions;
}
