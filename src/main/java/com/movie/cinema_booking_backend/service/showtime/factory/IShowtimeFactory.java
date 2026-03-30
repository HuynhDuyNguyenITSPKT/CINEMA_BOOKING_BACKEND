package com.movie.cinema_booking_backend.service.showtime.factory;

import com.movie.cinema_booking_backend.entity.Auditorium;
import com.movie.cinema_booking_backend.entity.Movie;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.response.ShowtimeResponse;

import java.time.LocalDateTime;

/**
 * IShowtimeFactory — Factory Pattern Interface cho Showtime.
 *
 * <p>Tập trung toàn bộ logic khởi tạo và mapping Showtime objects.
 * Service chỉ gọi factory, không tự build entity hay map DTO bằng tay.
 *
 * <p>SOLID — Single Responsibility: Factory chỉ chịu trách nhiệm tạo/map Showtime objects.
 * <p>SOLID — Dependency Inversion: ShowtimeService phụ thuộc vào interface này,
 * không phụ thuộc trực tiếp vào {@link com.movie.cinema_booking_backend.service.showtime.factory.impl.ShowtimeFactory}.
 * <p>Design Pattern — Factory: Che giấu chi tiết khởi tạo entity và mapping DTO.
 *
 * <p>Benefit:
 * <ul>
 *   <li>Dễ test: mock factory trong unit test</li>
 *   <li>Dễ thay đổi: sửa mapping logic không ảnh hưởng service</li>
 *   <li>Clean code: Service sạch, không lộn xộn với entity building</li>
 * </ul>
 */
public interface IShowtimeFactory {

    /**
     * Tạo Showtime Entity mới từ các thành phần đã được validate.
     *
     * <p>Service chỉ gọi method này sau khi đã validate movie, auditorium,
     * kiểm tra lịch trùng, và tính basePrice qua Strategy Pattern.
     *
     * @param movie      entity phim đã được tìm thấy trong DB
     * @param auditorium entity phòng chiếu đã được tìm thấy trong DB
     * @param startTime  thời gian bắt đầu buổi chiếu
     * @param endTime    thời gian kết thúc (bao gồm buffer dọn dẹp)
     * @param basePrice  giá vé đã được tính qua PricingStrategyContext
     * @return showtime entity (chưa được lưu vào DB)
     */
    Showtime createEntity(Movie movie, Auditorium auditorium,
                          LocalDateTime startTime, LocalDateTime endTime,
                          int basePrice);

    /**
     * Map Showtime Entity sang ShowtimeResponse DTO để trả về client.
     *
     * <p>Mapping entities → DTOs tại đây, không ở controller hay service.
     * Validate null đầu vào để đảm bảo không trả về null response.
     *
     * @param showtime showtime entity từ DB (không được null)
     * @return showtime DTO an toàn trả về client
     * @throws IllegalArgumentException nếu showtime là null
     */
    ShowtimeResponse createResponse(Showtime showtime);
}
