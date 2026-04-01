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

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(
        name = "exam_sections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_section_order_index_exam",
                        columnNames = {"order_index", "exam_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
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
