package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Homework;
import com.melikyan.academy.entity.HomeworkTranslation;
import com.melikyan.academy.entity.LessonTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface HomeworkTranslationRepository extends JpaRepository<HomeworkTranslation, UUID> {
    boolean existsByHomeworkIdAndCodeIgnoreCase(UUID homeworkId, String code);

    boolean existsByHomeworkIdAndCodeIgnoreCaseAndIdNot(UUID homeworkId, String code, UUID id);

    Optional<HomeworkTranslation> findByHomeworkIdAndCodeIgnoreCase(UUID homeworkId, String code);

    List<HomeworkTranslation> findByHomeworkId(UUID homeworkId);

    List<HomeworkTranslation> findByCodeIgnoreCase(String code);
}
