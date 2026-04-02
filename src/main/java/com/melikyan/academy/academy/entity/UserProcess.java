package com.melikyan.academy.academy.entity;

import com.melikyan.academy.academy.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_processes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_process_user_purchsable",
                        columnNames = {"user_id", "purchasable_id"}
                )
        }
)
public class UserProcess extends BaseEntity {
    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @Column(name = "score_accumulated", precision = 10, scale = 2)
    private BigDecimal scoreAccumulated;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "purchasable_id", nullable = false)
    private Purchasable purchasable;

    @Column(name = "last_accessed_at")
    private OffsetDateTime lastAccessedAt;
}
