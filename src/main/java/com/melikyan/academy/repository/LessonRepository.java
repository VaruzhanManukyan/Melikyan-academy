package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    boolean existsByCourseIdAndOrderIndex(UUID id, Integer orderIndex);

    boolean existsByCourseIdAndOrderIndexAndIdNot(UUID courseId, Integer orderIndex, UUID id);
}