package com.melikyan.academy.entity;

import lombok.AccessLevel;
import jakarta.persistence.*;
import com.melikyan.academy.entity.enums.PaymentMethod;
import com.melikyan.academy.entity.enums.TransactionType;
import com.melikyan.academy.entity.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_transaction_user_id", columnList = "user_id"),
                @Index(name = "idx_transaction_product_id", columnList = "product_id")
        }
)
public class Transaction {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", columnDefinition = "char(3)", nullable = false)
    @ColumnDefault("'USD'")
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "transaction")
    private List<ProductRegistration> productRegistrations;
}