package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.ShowtimeRequest;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * IShowtimeService - Contract cho toàn bộ business logic của Showtime.
 *
 * SOLID — Interface Segregation / Dependency Inversion: 
 * Lớp Facade và Controller phụ thuộc vào interface này, tuyệt đối không dùng trực tiếp Repository.
 */
public interface IShowtimeService {

    /**
     * Tạo lịch chiếu mới.
     * Áp dụng tính giá thông qua PricingStrategyContext (Strategy Pattern).
     *
     * @param request thông tin tạo lịch chiếu
     * @return ShowtimeResponse sau khi tạo hoàn tất
     */
    ShowtimeResponse createShowtime(ShowtimeRequest request);

    /**
     * Lấy danh sách lịch chiếu của một bộ phim cụ thể trong ngày.
     * Được PublicCinemaFacadeImpl lấy data trả về màn hình khách hàng.
     *
     * @param movieId ID của phim
     * @param date ngày cần tra cứu
     * @return Danh sách lịch chiếu được tạo sẵn DTO
     */
    List<ShowtimeResponse> getShowtimesByMovieAndDate(String movieId, LocalDate date);
}
