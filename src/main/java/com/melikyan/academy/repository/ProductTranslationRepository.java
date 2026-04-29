package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ProductTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductTranslationRepository extends JpaRepository<ProductTranslation, UUID> {
    boolean existsByProductIdAndCodeIgnoreCase(UUID productId, String code);

    boolean existsByProductIdAndCodeIgnoreCaseAndIdNot(UUID productId, String code, UUID id);

    Optional<ProductTranslation> findByProductIdAndCodeIgnoreCase(UUID productId, String code);

    List<ProductTranslation> findByProductId(UUID productId);

    List<ProductTranslation> findByCodeIgnoreCase(String code);
}
