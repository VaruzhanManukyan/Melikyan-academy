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
        name = "purchasable_translations",
        indexes = {
                @Index(name = "idx_purchasable_translation_code", columnList = "code"),
                @Index(name = "idx_purchasable_translation_purchasable_id", columnList = "purchasable_id")
        }
)
public class PurchasableTranslation extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(length = 2, name = "code", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "purchasable_id", nullable = false)
    private ContentItem contentItem;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
