package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.enums.ServiceCategory;
import com.movie.cinema_booking_backend.request.ExtraServiceRequest;
import com.movie.cinema_booking_backend.response.ExtraServiceResponse;
import org.springframework.data.domain.Page;

public interface IExtraServiceService {
    Page<ExtraServiceResponse> getUserExtraServices(int page, int size, ServiceCategory category);

    Page<ExtraServiceResponse> getAllExtraServicesForAdmin(int page, int size, Boolean isActive, ServiceCategory category);

    ExtraServiceResponse getExtraServiceById(Long id);

    ExtraServiceResponse createExtraService(ExtraServiceRequest request);

    ExtraServiceResponse updateExtraService(Long id, ExtraServiceRequest request);

    void deleteExtraService(Long id);
}
