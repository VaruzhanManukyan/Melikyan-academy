package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ProductContentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductContentItemRepository extends JpaRepository<ProductContentItem, UUID> {
    void deleteAllByProduct_Id(UUID productId);
}
