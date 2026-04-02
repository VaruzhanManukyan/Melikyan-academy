package com.melikyan.academy.academy.entity;

import com.melikyan.academy.academy.entity.base.BaseEntitySoftDelete;
import com.melikyan.academy.academy.entity.enums.PurchasableType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "purchasables",
        indexes = {
                @Index(name = "idx_purchasable_category_id", columnList = "category_id")
        }
)
public class Purchasable extends BaseEntitySoftDelete {
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PurchasableType type;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "purchasable")
    private List<UserProcess> userProcess;

    @OneToOne(mappedBy = "purchasable")
    private Course course;

    @OneToOne(mappedBy = "purchasable")
    private Exam exam;

    @OneToMany(mappedBy = "purchasable")
    private List<Certificate> certificates;

    @OneToMany(mappedBy = "purchasable")
    private List<ProductPurchasable> products;

    @OneToMany(mappedBy = "purchasable")
    private List<PurchasableTranslation> purchasableTranslations;
}
