package com.melikyan.academy.entity;

import lombok.AccessLevel;
import jakarta.persistence.*;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.base.BaseEntitySoftDelete;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "languages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
public class Language extends BaseEntitySoftDelete {
    @Id
    @Column(name = "code", columnDefinition = "char(2)", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
