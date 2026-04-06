package com.movie.cinema_booking_backend.repository;

import com.movie.cinema_booking_backend.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, String> {

    /**
     * Kiểm tra tên loại ghế đã tồn tại chưa (cho validation tạo mới).
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra tên loại ghế đã tồn tại, loại trừ id hiện tại (cho PUT).
     */
    boolean existsByNameAndIdNot(String name, String id);

    @Query("SELECT st, COUNT(s.id) FROM SeatType st LEFT JOIN st.seats s GROUP BY st ORDER BY st.name")
    List<Object[]> findAllWithSeatUsage();
}
