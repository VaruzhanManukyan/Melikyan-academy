package com.melikyan.academy.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SoftDeleteType;
import com.melikyan.academy.entity.base.BaseEntity;
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
@NoArgsConstructor
@Table(name = "languages")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
public class Language extends BaseEntity {
    @Column(length = 2, name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
