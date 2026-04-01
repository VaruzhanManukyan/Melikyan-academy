package com.melikyan.academy.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "exams",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_exam_purchasable",
                        columnNames = {"purchasable_id"}
                )
        }
)
public class Exam {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "purchasable_id", nullable = false)
    private Purchasable purchasable;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "exam")
    private List<ExamSection> examSections;
}
