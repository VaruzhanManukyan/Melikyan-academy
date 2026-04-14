package com.melikyan.academy.repository;

import com.melikyan.academy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByTitleIgnoreCase(String title);
}