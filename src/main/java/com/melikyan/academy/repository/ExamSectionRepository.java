package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ExamSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamSectionRepository extends JpaRepository<ExamSection, UUID> {
    List<ExamSection> findByExamIdOrderByOrderIndexAsc(UUID examId);

    boolean existsByExamIdAndOrderIndex(UUID examId, Integer orderIndex);

    boolean existsByExamIdAndTitleIgnoreCase(UUID examId, String title);

    boolean existsByExamIdAndTitleIgnoreCaseAndIdNot(UUID examId, String title, UUID examSectionId);

    boolean existsByExamIdAndOrderIndexAndIdNot(UUID examId, Integer orderIndex, UUID examSectionId);
}
