package com.melikyan.academy.entity;

import lombok.AccessLevel;
import jakarta.persistence.*;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;

import java.util.List;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "homeworks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_homework_order_index_lesson",
                        columnNames = {"lesson_id", "order_index"}
                )
        }
)
public class Homework extends BaseEntitySoftDelete {
    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date", nullable = false)
    private OffsetDateTime dueDate;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "homework")
    private List<HomeworkTask> homeworkTasks;
}