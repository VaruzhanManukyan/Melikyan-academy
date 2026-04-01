package com.melikyan.academy.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "duration_weeks", nullable = false)
    private Integer durationWeeks;

    @OneToOne
    @JoinColumn(name = "purchasable_id", nullable = false)
    private Purchasable purchasable;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "course")
    private List<Professor> professors;

    @OneToMany(mappedBy = "course")
    private List<Lesson> lessons;


}
