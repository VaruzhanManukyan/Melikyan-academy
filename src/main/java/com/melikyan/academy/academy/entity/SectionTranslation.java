package com.melikyan.academy.academy.entity;

import com.melikyan.academy.academy.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "section_translations",
        indexes = {
                @Index(name = "idx_section_translation_code", columnList = "code"),
                @Index(name = "idx_section_translation_section_id", columnList = "section_id")
        }
)
public class SectionTranslation extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "code", columnDefinition = "char(2)", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private ExamSection examSection;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}