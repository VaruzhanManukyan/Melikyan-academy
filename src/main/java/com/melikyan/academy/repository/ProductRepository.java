package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @EntityGraph(attributePaths = {
            "category",
            "createdBy",
            "contentItems",
            "contentItems.contentItem"
    })
    Optional<Product> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {
            "category",
            "createdBy",
            "contentItems",
            "contentItems.contentItem"
    })
    List<Product> findAllBy();

    boolean existsByTitleIgnoreCase(String title);

    Product findByTitleIgnoreCase(String title);
}
