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

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
public class Product extends BaseEntitySoftDelete {
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PurchasableType type;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "product")
    private List<ProductPurchasable> purchasables;

    @OneToMany(mappedBy = "product")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "product")
    private List<ProductRegistration> productRegistrations;
}