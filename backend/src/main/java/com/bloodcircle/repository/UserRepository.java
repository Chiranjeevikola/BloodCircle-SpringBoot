package com.bloodcircle.repository;

import com.bloodcircle.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByIsBlockedTrue();

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    // Filters for admin manage users
    Page<User> findByDeletedAtIsNull(Pageable pageable);

    Page<User> findByDeletedAtIsNullAndRole(String role, Pageable pageable);

    Page<User> findByDeletedAtIsNullAndIsActiveTrue(Pageable pageable);

    Page<User> findByDeletedAtIsNullAndIsActiveFalse(Pageable pageable);

    Page<User> findByDeletedAtIsNullAndEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<User> findByDeletedAtIsNullAndRoleAndEmailContainingIgnoreCase(String role, String email, Pageable pageable);
}
