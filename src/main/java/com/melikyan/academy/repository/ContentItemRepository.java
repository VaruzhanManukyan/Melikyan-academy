package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ContentItem;
import com.melikyan.academy.entity.enums.ContentItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

@Repository
public interface ContentItemRepository extends JpaRepository<ContentItem, UUID> {
    boolean existsByTypeAndTitleIgnoreCase(ContentItemType type, String title);

    boolean existsByTypeAndTitleIgnoreCaseAndIdNot(ContentItemType type, String title, UUID id);


    @Modifying(flushAutomatically = true)
    @Query("""
        UPDATE ContentItem content_item
        SET content_item.totalSteps = content_item.totalSteps + :delta
        WHERE content_item.id = :contentItemId
        AND content_item.totalSteps + :delta >= 0
    """)
    int changeTotalSteps(
            @Param("contentItemId") UUID contentItemId,
            @Param("delta") int delta
    );
}

