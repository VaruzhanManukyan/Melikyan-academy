package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    boolean existsByCourseIdAndOrderIndex(UUID courseId, Integer orderIndex);

    boolean existsByCourseIdAndTitleIgnoreCase(UUID courseId, String title);

    boolean existsByCourseIdAndTitleIgnoreCaseAndIdNot(UUID courseId, String title, UUID lessonId);

    boolean existsByCourseIdAndOrderIndexAndIdNot(UUID courseId, Integer orderIndex, UUID lessonId);

    @EntityGraph(attributePaths = {
            "course",
            "course.contentItem"
    })
    Optional<Lesson> findDetailedById(UUID id);
}