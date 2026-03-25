package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Page<Promotion> findByIsActive(boolean isActive, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, String id);
}
