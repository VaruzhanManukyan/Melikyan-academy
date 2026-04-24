package com.melikyan.academy.repository;

import com.melikyan.academy.entity.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, UUID> {
    Optional<HomeworkSubmission> findByIdAndUserId(UUID id, UUID userId);

    Optional<HomeworkSubmission> findByUserIdAndTaskId(UUID userId, UUID taskId);

    List<HomeworkSubmission> findAllByUserId(UUID userId);

    List<HomeworkSubmission> findAllByTaskId(UUID taskId);

    boolean existsByUserIdAndTaskId(UUID userId, UUID taskId);
}