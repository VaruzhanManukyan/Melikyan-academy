package com.melikyan.academy.repository;

import com.melikyan.academy.entity.UserProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface UserProcessRepository extends JpaRepository<UserProcess, UUID> {

    @EntityGraph(attributePaths = {
            "user",
            "contentItem"
    })
    List<UserProcess> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {
            "user",
            "contentItem"
    })
    Optional<UserProcess> findByUserIdAndContentItemId(UUID userId, UUID contentItemId);

    boolean existsByUserIdAndContentItemId(UUID userId, UUID contentItemId);
}