package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    boolean existsByCourseIdAndOrderIndex(UUID id, Integer orderIndex);

    boolean existsByCourseIdAndOrderIndexAndIdNot(UUID courseId, Integer orderIndex, UUID id);

    List<Lesson> findAllByCourseIdOrderByOrderIndexAsc(UUID courseId);
}