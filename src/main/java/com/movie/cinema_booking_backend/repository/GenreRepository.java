package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * GenreRepository — Data access layer cho Genre entity.
 *
 * <p>existsByNameIgnoreCase đảm bảo không tạo trùng tên thể loại
 * ("Action" và "action" là cùng một — nghiêng về góc nhìn user).
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, String> {

    boolean existsByNameIgnoreCase(String name);
}
