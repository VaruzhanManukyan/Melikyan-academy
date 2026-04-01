package com.melikyan.academy.entity;

import lombok.AccessLevel;
import jakarta.persistence.*;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "product_registrations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_registration_user_product",
                        columnNames = {"user_id", "product_id"}
                )
        },
        indexes = {
                @Index(name = "idx_product_registration_transaction_id", columnList = "transaction_id")
        }
)
public class ProductRegistration extends BaseEntitySoftDelete {
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
}
