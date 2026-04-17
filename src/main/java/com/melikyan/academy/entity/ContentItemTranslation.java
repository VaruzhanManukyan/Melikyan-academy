package com.melikyan.academy.entity;

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
@NoArgsConstructor
@Table(
        name = "content_item_translations",
        indexes = {
                @Index(name = "idx_content_item_translation_code", columnList = "code"),
                @Index(name = "idx_content_item_translation_content_item_id", columnList = "content_item_id")
        }
)
public class ContentItemTranslation extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(length = 2, name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "content_item_id", nullable = false)
    private ContentItem contentItem;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
