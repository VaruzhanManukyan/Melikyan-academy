package com.melikyan.academy.repository;

import com.melikyan.academy.entity.User;
import com.melikyan.academy.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String username);

    boolean existsByEmail(String email);

    List<User> findAllByRoleIn(Collection<Role> roles);
}
