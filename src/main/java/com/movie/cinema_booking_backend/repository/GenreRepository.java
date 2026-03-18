package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, String> {
    Page findAll(Pageable pageable);

    boolean existsByName(String name);
}
