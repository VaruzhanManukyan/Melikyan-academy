package com.melikyan.academy.repository;

import com.melikyan.academy.entity.SectionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SectionTranslationRepository extends JpaRepository<SectionTranslation, UUID> {
    boolean existsByExamSectionIdAndCodeIgnoreCase(UUID examSectionId, String code);

    boolean existsByExamSectionIdAndCodeIgnoreCaseAndIdNot(UUID examSectionId, String code, UUID id);

    Optional<SectionTranslation> findByExamSectionIdAndCodeIgnoreCase(UUID examSectionId, String code);

    List<SectionTranslation> findByExamSectionId(UUID examSectionId);

    List<SectionTranslation> findByCodeIgnoreCase(String code);
}
