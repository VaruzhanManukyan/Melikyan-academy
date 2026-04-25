package com.melikyan.academy.repository;

import com.melikyan.academy.entity.LessonAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface LessonAttendanceRepository extends JpaRepository<LessonAttendance, UUID> {
    Optional<LessonAttendance> findByIdAndUserId(UUID id, UUID userId);

    Optional<LessonAttendance> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<LessonAttendance> findAllByUserId(UUID userId);

    List<LessonAttendance> findAllByLessonId(UUID lessonId);

    boolean existsByUserIdAndLessonId(UUID userId, UUID lessonId);
}