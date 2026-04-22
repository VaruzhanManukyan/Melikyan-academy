package com.melikyan.academy.entity;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.base.BaseEntity;
import com.melikyan.academy.entity.enums.ContentItemType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "content_items")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
public class ContentItem extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false)
    private ContentItemType type;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @ManyToOne
    @JoinColumn(name = "created_by",  nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "contentItem")
    private List<UserProcess> userProcess;

    @OneToOne(mappedBy = "contentItem", fetch = FetchType.LAZY)
    private Course course;

    @OneToOne(mappedBy = "contentItem", fetch = FetchType.LAZY)
    private Exam exam;

    @OneToMany(mappedBy = "contentItem")
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "contentItem")
    private List<ProductContentItem> products;

    @OneToMany(mappedBy = "contentItem")
    private List<ContentItemTranslation> contentItemTranslations;
}
