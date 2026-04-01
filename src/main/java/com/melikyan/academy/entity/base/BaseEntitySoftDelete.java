package com.melikyan.academy.entity.base;

import lombok.AccessLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntitySoftDelete extends BaseEntity {
    @Column(name = "deleted_at", insertable = false, updatable = false)
    private OffsetDateTime deletedAt;
}