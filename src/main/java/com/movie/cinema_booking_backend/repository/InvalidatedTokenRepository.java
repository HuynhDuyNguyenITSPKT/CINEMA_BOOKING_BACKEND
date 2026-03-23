package com.movie.cinema_booking_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.movie.cinema_booking_backend.entity.InvalidatedToken;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {
    boolean existsById(String jwtid);
    Optional<InvalidatedToken> findById(String jwtid);
}
