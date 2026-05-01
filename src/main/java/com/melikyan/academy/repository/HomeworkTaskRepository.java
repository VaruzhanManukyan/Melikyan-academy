package com.melikyan.academy.repository;

import com.melikyan.academy.entity.HomeworkTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HomeworkTaskRepository extends JpaRepository<HomeworkTask, UUID> {
    List<HomeworkTask> findAllByLessonIdOrderByOrderIndexAsc(UUID lessonId);

    boolean existsByLessonIdAndOrderIndex(UUID lessonId, Integer orderIndex);

    boolean existsByLessonIdAndOrderIndexAndIdNot(UUID lessonId, Integer orderIndex, UUID homeworkTaskId);
}