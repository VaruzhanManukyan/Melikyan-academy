package com.melikyan.academy.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.enums.PurchasableType;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;

import java.util.List;
import java.math.BigDecimal;

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

    @OneToMany(mappedBy = "product")
    private List<ProductPurchasable> purchasables;

    @OneToMany(mappedBy = "product")
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "product")
    private List<ProductRegistration> productRegistrations;
}