package com.melikyan.academy.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_content_item",
                        columnNames = {"content_item_id"}
                )
        }
)
public class Course {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "duration_weeks", nullable = false)
    private Integer durationWeeks;

    @OneToOne
    @JoinColumn(name = "content_item_id", nullable = false)
    private ContentItem contentItem;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "course")
    private List<Professor> professors;

    @OneToMany(mappedBy = "course")
    private List<Lesson> lessons;
}
