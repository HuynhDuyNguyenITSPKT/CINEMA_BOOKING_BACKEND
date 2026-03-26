package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.ExtraService;
import com.movie.cinema_booking_backend.enums.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraServiceRepository extends JpaRepository<ExtraService, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<ExtraService> findByIsActive(Boolean isActive, Pageable pageable);

    Page<ExtraService> findByCategory(ServiceCategory category, Pageable pageable);

    Page<ExtraService> findByIsActiveAndCategory(Boolean isActive, ServiceCategory category, Pageable pageable);
}
