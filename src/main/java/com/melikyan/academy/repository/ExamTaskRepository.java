package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ExamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamTaskRepository extends JpaRepository<ExamTask, UUID> {
    List<ExamTask> findAllBySectionIdOrderByOrderIndexAsc(UUID examSectionId);

    boolean existsBySectionIdAndOrderIndex(UUID examSectionId, Integer orderIndex);

    boolean existsBySectionIdAndOrderIndexAndIdNot(UUID examSectionId, Integer orderIndex, UUID examId);
}
