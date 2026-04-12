package com.melikyan.academy.entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.base.BaseEntity;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.Map;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Table(
        name = "certificates",
        indexes = {
                @Index(name = "idx_certificate_user_id", columnList = "user_id"),
                @Index(name = "idx_certificate_purchasable_id", columnList = "purchasable_id")
        }
)
public class Certificate extends BaseEntity {
    @Column(name = "certificate_code", nullable = false)
    private String certificateCode;

    @Column(name = "issue_date", nullable = false)
    private OffsetDateTime issueDate;

    @Column(name = "expiry_date")
    private OffsetDateTime expiryDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metadata;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "purchasable_id", nullable = false)
    private Purchasable purchasable;
}