package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.AuditoriumRequest;
import com.movie.cinema_booking_backend.response.AuditoriumResponse;

import java.util.List;

public interface IAuditoriumService {
    List<AuditoriumResponse> getAllAuditoriums();
    AuditoriumResponse getAuditoriumById(String id);
    AuditoriumResponse createAuditorium(AuditoriumRequest request);
    AuditoriumResponse updateAuditorium(String id, AuditoriumRequest request);
    void deleteAuditorium(String id);
    /** Xoá toàn bộ ghế cũ và generate lại theo layout mới (khi sửa phòng vật lý). */
    AuditoriumResponse regenerateSeats(String id, AuditoriumRequest request);
}
