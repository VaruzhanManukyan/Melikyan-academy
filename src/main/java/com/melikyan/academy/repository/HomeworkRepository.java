package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, UUID> {
    boolean existsByLessonIdAndOrderIndex(UUID id, Integer orderIndex);

    boolean existsByLessonIdAndOrderIndexAndIdNot(UUID lessonId, Integer orderIndex, UUID id);
}
