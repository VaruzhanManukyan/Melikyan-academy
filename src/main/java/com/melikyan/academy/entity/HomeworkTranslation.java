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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "homework_translations",
        indexes = {
                @Index(name = "idx_homework_translation_homework_id", columnList = "homework_id")
        }
)
public class HomeworkTranslation extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "code", columnDefinition = "char(2)", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}