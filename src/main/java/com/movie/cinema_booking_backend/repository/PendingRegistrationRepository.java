package com.movie.cinema_booking_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.movie.cinema_booking_backend.entity.PendingRegistration;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    PendingRegistration findByEmail(String email);

    void deleteByUsername(String username);

    void deleteByEmail(String email);

}
