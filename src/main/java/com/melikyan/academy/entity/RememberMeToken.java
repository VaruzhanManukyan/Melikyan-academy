package com.melikyan.academy.entity;

import lombok.*;
import jakarta.persistence.*;
import com.melikyan.academy.entity.base.BaseEntity;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "remember_me_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_remember_me_token_selector",
                        columnNames = {"selector"}
                ),
                @UniqueConstraint(
                        name = "uk_remember_me_token_user",
                        columnNames = {"user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_remember_me_token_user_id", columnList = "user_id"),
                @Index(name = "idx_remember_me_token_expires_at", columnList = "expires_at")
        }
)
public class RememberMeToken extends BaseEntity {
    @Column(name = "selector", nullable = false, length = 64)
    private String selector;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "last_used_at", nullable = false)
    private OffsetDateTime lastUsedAt;
}
