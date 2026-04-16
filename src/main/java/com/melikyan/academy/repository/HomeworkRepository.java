package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, UUID> {
    List<Homework> findByLessonIdOrderByOrderIndexDesc(UUID lessonId);

    boolean existsByLessonIdAndOrderIndex(UUID id, Integer orderIndex);

    boolean existsByLessonIdAndTitleIgnoreCase(UUID lessonId, String title);

    boolean existsByLessonIdAndTitleIgnoreCaseAndIdNot(UUID lessonId, String title, UUID id);

    boolean existsByLessonIdAndOrderIndexAndIdNot(UUID lessonId, Integer orderIndex, UUID id);
}
