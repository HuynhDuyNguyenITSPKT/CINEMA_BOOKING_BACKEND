package com.movie.cinema_booking_backend.service.showtime.factory.impl;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;
import com.movie.cinema_booking_backend.service.showtime.factory.IShowtimeFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ShowtimeFactory — Factory Pattern Implementation cho Showtime.
 *
 * <p>Tập trung toàn bộ mapping logic tại đây.
 * Service ShowtimeService chỉ gọi các method này, không tự build entity hay map DTO bằng tay.
 *
 * <p>SOLID — Single Responsibility: Chỉ chịu trách nhiệm tạo/map Showtime objects.
 * <p>OOP — Encapsulation: Logic khởi tạo được đóng gói hoàn toàn tại đây.
 */
@Component
public class ShowtimeFactory implements IShowtimeFactory {

    /**
     * Tạo Showtime Entity từ các thành phần đã được validate.
     *
     * @param movie      entity phim đã được tìm thấy trong DB (không null)
     * @param auditorium entity phòng chiếu đã được tìm thấy trong DB (không null)
     * @param startTime  thời gian bắt đầu buổi chiếu
     * @param endTime    thời gian kết thúc (bao gồm buffer dọn dẹp)
     * @param basePrice  giá vé đã được tính qua PricingStrategyContext
     * @return showtime entity (chưa lưu vào DB)
     */
    @Override
    public Showtime createEntity(Movie movie, Auditorium auditorium,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 int basePrice) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie không thể null khi tạo Showtime");
        }
        if (auditorium == null) {
            throw new IllegalArgumentException("Auditorium không thể null khi tạo Showtime");
        }
        return Showtime.builder()
                .movie(movie)
                .auditorium(auditorium)
                .startTime(startTime)
                .endTime(endTime)
                .basePrice(basePrice)
                .build();
    }

    /**
     * Map Showtime Entity sang ShowtimeResponse DTO để trả về client.
     * Validate null đầu vào để đảm bảo không trả về null response.
     *
     * @param showtime showtime entity từ DB (không được null)
     * @return showtime DTO an toàn trả về client
     */
    @Override
    public ShowtimeResponse createResponse(Showtime showtime) {
        if (showtime == null) {
            throw new IllegalArgumentException("Showtime không thể null");
        }
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovie() != null ? showtime.getMovie().getId() : null)
                .movieTitle(showtime.getMovie() != null ? showtime.getMovie().getTitle() : null)
                .auditoriumId(showtime.getAuditorium() != null ? showtime.getAuditorium().getId() : null)
                .auditoriumName(showtime.getAuditorium() != null ? showtime.getAuditorium().getName() : null)
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .build();
    }
}
