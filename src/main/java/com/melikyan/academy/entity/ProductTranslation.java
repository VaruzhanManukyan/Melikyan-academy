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
        name = "product_translations",
        indexes = {
                @Index(name = "idx_product_translation_code", columnList = "code"),
                @Index(name = "idx_product_translation_product_id", columnList = "product_id")
        }
)
public class ProductTranslation extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "code", columnDefinition = "char(2)", nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
