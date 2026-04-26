package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    Optional<Professor> findByUserIdAndCourseId(UUID userId, UUID courseId);

    List<Professor> findAllByUserId(UUID userId);

    List<Professor> findAllByCourseId(UUID courseId);
}