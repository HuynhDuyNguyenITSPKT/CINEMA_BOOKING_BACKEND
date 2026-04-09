package com.movie.cinema_booking_backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.movie.cinema_booking_backend.entity.Account;
import com.movie.cinema_booking_backend.enums.Role;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByUsername(String username);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByUserEmail(String email);

    Optional<Account> findByUserId(Long userId);

    long countByRole(Role role);

    Page<Account> findByUsernameContainingIgnoreCaseOrUser_FullNameContainingIgnoreCase(
            String username,
            String fullName,
            Pageable pageable
    );
}
