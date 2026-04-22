package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, UUID> {
    List<Homework> findByLessonIdOrderByOrderIndexAsc(UUID lessonId);

    boolean existsByLessonIdAndOrderIndex(UUID id, Integer orderIndex);

    boolean existsByLessonIdAndTitleIgnoreCase(UUID lessonId, String title);

    boolean existsByLessonIdAndTitleIgnoreCaseAndIdNot(UUID lessonId, String title, UUID id);

    boolean existsByLessonIdAndOrderIndexAndIdNot(UUID lessonId, Integer orderIndex, UUID id);

    @EntityGraph(attributePaths = {
            "lesson",
            "lesson.course",
            "lesson.course.contentItem"
    })
    Optional<Homework> findDetailedById(UUID id);
}
