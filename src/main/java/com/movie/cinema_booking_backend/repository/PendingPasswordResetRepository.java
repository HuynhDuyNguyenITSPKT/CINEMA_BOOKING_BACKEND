package com.movie.cinema_booking_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.movie.cinema_booking_backend.entity.PendingPasswordReset;

@Repository
public interface PendingPasswordResetRepository extends JpaRepository<PendingPasswordReset, Long> {
    PendingPasswordReset findByEmail(String email);

    void deleteByEmail(String email);
}
