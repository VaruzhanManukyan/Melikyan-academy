package com.melikyan.academy.repository;

import com.melikyan.academy.entity.ExamSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, UUID> {
    Optional<ExamSubmission> findByIdAndUserId(UUID id, UUID userId);

    Optional<ExamSubmission> findByUserIdAndTaskId(UUID userId, UUID taskId);

    List<ExamSubmission> findAllByUserId(UUID userId);

    List<ExamSubmission> findAllByTaskId(UUID taskId);

    boolean existsByUserIdAndTaskId(UUID userId, UUID taskId);
}
