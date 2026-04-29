package com.melikyan.academy.repository;

import com.melikyan.academy.entity.LessonTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface LessonTranslationRepository extends JpaRepository<LessonTranslation, UUID> {
    boolean existsByLessonIdAndCodeIgnoreCase(UUID lessonId, String code);

    boolean existsByLessonIdAndCodeIgnoreCaseAndIdNot(UUID lessonId, String code, UUID id);

    Optional<LessonTranslation> findByLessonIdAndCodeIgnoreCase(UUID lessonId, String code);

    List<LessonTranslation> findByLessonId(UUID lessonId);

    List<LessonTranslation> findByCodeIgnoreCase(String code);
}