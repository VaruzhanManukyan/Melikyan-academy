package com.melikyan.academy.repository;

import com.melikyan.academy.entity.RememberMeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, UUID> {
    @Query("""
        SELECT remember_me_token
        FROM RememberMeToken remember_me_token
        JOIN FETCH remember_me_token.user
        WHERE remember_me_token.selector = :selector
    """)
    Optional<RememberMeToken> findBySelectorWithUser(@Param("selector")  String selector);

    Optional<RememberMeToken> findBySelector(String selector);

    void deleteBySelector(String selector);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        DELETE FROM RememberMeToken remember_me_token
        WHERE remember_me_token.user.id = :userId
    """)
    void deleteByUserId(@Param("userId") UUID userId);

    void deleteAllByExpiresAtBefore(OffsetDateTime now);
}
