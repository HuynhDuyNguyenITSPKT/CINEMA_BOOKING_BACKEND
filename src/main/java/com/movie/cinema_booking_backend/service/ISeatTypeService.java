package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.request.SeatTypeRequest;
import com.movie.cinema_booking_backend.response.SeatTypeAdminResponse;
import com.movie.cinema_booking_backend.response.SeatTypeResponse;

import java.util.List;

public interface ISeatTypeService {
    List<SeatTypeResponse> getAllSeatTypes();
    List<SeatTypeAdminResponse> getAllSeatTypesForAdmin();
    SeatTypeResponse getSeatTypeById(String id);
    SeatTypeResponse createSeatType(SeatTypeRequest request);
    SeatTypeResponse updateSeatType(String id, SeatTypeRequest request);
    void deleteSeatType(String id);
}
