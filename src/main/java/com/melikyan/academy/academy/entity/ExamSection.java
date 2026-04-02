package com.melikyan.academy.academy.entity;

import com.melikyan.academy.academy.entity.base.BaseEntitySoftDelete;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "exam_sections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_section_order_index_exam",
                        columnNames = {"order_index", "exam_id"}
                )
        },
        indexes = {
                @Index(name = "idx_exam_section_exam_id", columnList = "exam_id"),
        }
)
public class ExamSection extends BaseEntitySoftDelete {
    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "duration", columnDefinition = "Interval")
    private Duration duration;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "section")
    private List<ExamTask> examTasks;

    @OneToMany(mappedBy = "examSection")
    private List<SectionTranslation> sections;
}
