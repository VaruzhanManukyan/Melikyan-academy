package com.melikyan.academy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.UUID;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        name = "product_purchasables",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_purchasable",
                        columnNames = {"product_id", "purchasable_id"}
                )
        }
)
public class ProductPurchasable {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "purchasable_id", nullable = false)
    private ContentItem contentItem;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
