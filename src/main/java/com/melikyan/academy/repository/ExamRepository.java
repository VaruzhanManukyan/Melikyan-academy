package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {
    @EntityGraph(attributePaths = {
            "contentItem",
            "contentItem.createdBy"
    })
    @Query("SELECT exam FROM Exam exam WHERE exam.id = :id")
    Optional<Exam> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {
            "contentItem",
            "contentItem.createdBy"
    })
    @Query("SELECT exam FROM Exam exam")
    List<Exam> findAllDetailed();
}
