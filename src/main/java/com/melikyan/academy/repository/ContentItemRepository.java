package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ContentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContentItemRepository extends JpaRepository<ContentItem, UUID> {
    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, UUID id);
}

