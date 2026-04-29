package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ContentItemTranslation;
import com.melikyan.academy.entity.enums.ContentItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentItemTranslationRepository extends JpaRepository<ContentItemTranslation, UUID> {
    boolean existsByContentItemIdAndCodeIgnoreCase(UUID contentItemId, String code);

    boolean existsByContentItemIdAndCodeIgnoreCaseAndIdNot(UUID contentItemId, String code, UUID id);

    Optional<ContentItemTranslation> findByContentItemIdAndCodeIgnoreCase(UUID contentItemId, String code);

    List<ContentItemTranslation> findByContentItemId(UUID contentItemId);

    List<ContentItemTranslation> findByContentItemType(ContentItemType type);

    List<ContentItemTranslation> findByContentItemTypeAndCodeIgnoreCase(ContentItemType type, String code);
}