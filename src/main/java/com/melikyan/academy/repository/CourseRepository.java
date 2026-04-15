package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    @EntityGraph(attributePaths = {
            "purchasable",
            "purchasable.category",
            "purchasable.createdBy"
    })
    @Query("SELECT course FROM Course course WHERE course.id = :id")
    Optional<Course> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {
            "purchasable",
            "purchasable.category",
            "purchasable.createdBy"
    })
    @Query("SELECT course FROM Course course")
    List<Course> findAllDetailed();
}
