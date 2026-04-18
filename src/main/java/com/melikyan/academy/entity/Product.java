package com.melikyan.academy.entity;

import com.melikyan.academy.entity.enums.ProductType;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.base.BaseEntity;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.List;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_category_id", columnList = "category_id")
        }
)
public class Product extends BaseEntity {
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type")
    private ProductType type;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "product")
    private List<ProductContentItem> contentItems;

    @OneToMany(mappedBy = "product")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "product")
    private List<ProductRegistration> productRegistrations;
}