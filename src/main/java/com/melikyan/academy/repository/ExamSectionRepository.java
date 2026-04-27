package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ExamSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ExamSectionRepository extends JpaRepository<ExamSection, UUID> {
    List<ExamSection> findByExamIdOrderByOrderIndexAsc(UUID examId);

    boolean existsByExamIdAndOrderIndex(UUID examId, Integer orderIndex);

    boolean existsByExamIdAndTitleIgnoreCase(UUID examId, String title);

    boolean existsByExamIdAndTitleIgnoreCaseAndIdNot(UUID examId, String title, UUID examSectionId);

    boolean existsByExamIdAndOrderIndexAndIdNot(UUID examId, Integer orderIndex, UUID examSectionId);

    @EntityGraph(attributePaths = {
            "exam",
            "exam.contentItem",
            "createdBy"
    })
    Optional<ExamSection> findDetailedById(UUID id);
}
