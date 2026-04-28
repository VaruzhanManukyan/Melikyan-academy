package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ProductRegistration;
import com.melikyan.academy.entity.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<UUID> findUserIdsByContentItemIdAndStatus(
            @Param("contentItemId") UUID contentItemId,
            @Param("status") RegistrationStatus status
    );

    @Query("""
            SELECT COUNT(productRegistration) > 0
            FROM ProductRegistration productRegistration
            JOIN productRegistration.product product
            JOIN product.contentItems productContentItem
            WHERE productRegistration.user.id = :userId
              AND productContentItem.contentItem.id = :contentItemId
              AND productRegistration.status = :status
            """)
    boolean existsByUserIdAndContentItemIdAndStatus(
            @Param("userId") UUID userId,
            @Param("contentItemId") UUID contentItemId,
            @Param("status") RegistrationStatus status
    );

    boolean existsByUserIdAndProductIdAndStatus(
            UUID userId,
            UUID productId,
            RegistrationStatus status
    );

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM product_registrations pr
            JOIN product_content_items pci
                ON pci.product_id = pr.product_id
            WHERE pr.user_id = :userId
              AND pci.content_item_id = :contentItemId
              AND pr.status = 'ACTIVE'
              AND pr.deleted_at IS NULL
        )
        """, nativeQuery = true)
    boolean existsActiveRegistrationByUserIdAndContentItemId(
            UUID userId,
            UUID contentItemId
    );
}