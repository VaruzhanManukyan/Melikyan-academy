package com.melikyan.academy.entity;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.enums.TaskType;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.Map;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "homework_tasks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_homework_task_order_index_homework",
                        columnNames = {"homework_id", "order_index"}
                )
        }
)
public class HomeworkTask extends BaseEntitySoftDelete {
    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "point", nullable = false)
    private int point;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type")
    private TaskType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_content", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payloadContent;

    @ManyToOne
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "task")
    private List<HomeworkSubmission> homeworkSubmissions;
}