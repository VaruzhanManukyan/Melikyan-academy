package com.melikyan.academy.entity;

import lombok.AccessLevel;
import jakarta.persistence.*;
import com.melikyan.academy.entity.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "section_translations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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