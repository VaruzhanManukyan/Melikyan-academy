package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByCertificateCodeIgnoreCase(String certificateCode);

    boolean existsByCertificateCodeIgnoreCase(String certificateCode);

    boolean existsByUserIdAndContentItemId(UUID userId, UUID contentItemId);

    List<Certificate> findAllByUserId(UUID userId);

    List<Certificate> findAllByContentItemId(UUID contentItemId);
}