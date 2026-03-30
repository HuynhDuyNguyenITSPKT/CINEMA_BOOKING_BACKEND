package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.enums.AuditoriumStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriumRepository extends JpaRepository<Auditorium, String> {

    /**
     * Kiểm tra tên phòng chiếu đã tồn tại chưa (cho validation tạo/cập nhật).
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra tên phòng chiếu đã tồn tại, loại trừ id hiện tại (cho PUT).
     */
    boolean existsByNameAndIdNot(String name, String id);

    /**
     * Lọc phòng chiếu theo trạng thái (dùng cho admin filter hoặc Phase 4 guard).
     */
    List<Auditorium> findAllByStatus(AuditoriumStatus status);
}
