package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ProductRegistration;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ProductRegistrationRepository extends JpaRepository<ProductRegistration, UUID> {
    @EntityGraph(attributePaths = {
            "user",
            "product",
            "transaction"
    })
    Optional<ProductRegistration> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {
            "user",
            "product",
            "transaction"
    })
    List<ProductRegistration> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {
            "user",
            "product",
            "transaction"
    })
    List<ProductRegistration> findAllByProductId(UUID productId);

    @Query("""
            SELECT DISTINCT productRegistration.user.id
            FROM ProductRegistration productRegistration
            JOIN productRegistration.product product
            JOIN product.contentItems productContentItem
            WHERE productContentItem.contentItem.id = :contentItemId
            AND productRegistration.status = :status
            """)
    List<UUID> findUserIdsByContentItemIdAndStatus(UUID contentItemId, RegistrationStatus status);

    boolean existsByUserIdAndProductIdAndStatus(
            UUID userId,
            UUID productId,
            RegistrationStatus status
    );
}