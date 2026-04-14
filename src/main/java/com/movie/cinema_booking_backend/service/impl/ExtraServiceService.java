package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.ExtraService;
import com.movie.cinema_booking_backend.enums.ServiceCategory;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.ExtraServiceRepository;
import com.movie.cinema_booking_backend.request.ExtraServiceRequest;
import com.movie.cinema_booking_backend.response.ExtraServiceResponse;
import com.movie.cinema_booking_backend.service.IExtraServiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ExtraServiceService implements IExtraServiceService {

    private final ExtraServiceRepository extraServiceRepository;

    public ExtraServiceService(ExtraServiceRepository extraServiceRepository) {
        this.extraServiceRepository = extraServiceRepository;
    }

    @Override
    public Page<ExtraServiceResponse> getUserExtraServices(int page, int size, ServiceCategory category) {
        var pageable = PageRequest.of(page, size);
        Page<ExtraService> result = category == null
                ? extraServiceRepository.findByIsActive(true, pageable)
                : extraServiceRepository.findByIsActiveAndCategory(true, category, pageable);

        return result.map(this::toResponse);
    }

    @Override
    public Page<ExtraServiceResponse> getAllExtraServicesForAdmin(int page, int size, Boolean isActive, ServiceCategory category) {
        var pageable = PageRequest.of(page, size);
        Page<ExtraService> result;

        if (isActive != null && category != null) {
            result = extraServiceRepository.findByIsActiveAndCategory(isActive, category, pageable);
        } else if (isActive != null) {
            result = extraServiceRepository.findByIsActive(isActive, pageable);
        } else if (category != null) {
            result = extraServiceRepository.findByCategory(category, pageable);
        } else {
            result = extraServiceRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    @Override
    public ExtraServiceResponse getExtraServiceById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    public ExtraServiceResponse createExtraService(ExtraServiceRequest request) {
        if (extraServiceRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.EXTRA_SERVICE_NAME_EXISTS);
        }

        ExtraService extraService = ExtraService.builder()
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .unitPrice(request.getUnitPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return toResponse(extraServiceRepository.save(extraService));
    }

    @Override
    public ExtraServiceResponse updateExtraService(Long id, ExtraServiceRequest request) {
        ExtraService extraService = findByIdOrThrow(id);

        if (extraServiceRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.EXTRA_SERVICE_NAME_EXISTS);
        }

        extraService.setName(request.getName());
        extraService.setImageUrl(request.getImageUrl());
        extraService.setUnitPrice(request.getUnitPrice());
        extraService.setDescription(request.getDescription());
        extraService.setCategory(request.getCategory());
        extraService.setIsActive(request.getIsActive() != null ? request.getIsActive() : extraService.getIsActive());

        return toResponse(extraServiceRepository.save(extraService));
    }

    @Override
    public void deleteExtraService(Long id) {
        ExtraService extraService = findByIdOrThrow(id);
        extraServiceRepository.delete(extraService);
    }

    private ExtraService findByIdOrThrow(Long id) {
        return extraServiceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXTRA_SERVICE_NOT_FOUND));
    }

    private ExtraServiceResponse toResponse(ExtraService extraService) {
        return ExtraServiceResponse.builder()
                .id(extraService.getId())
                .name(extraService.getName())
                .imageUrl(extraService.getImageUrl())
                .unitPrice(extraService.getUnitPrice())
                .description(extraService.getDescription())
                .category(extraService.getCategory())
                .isActive(extraService.getIsActive())
                .build();
    }
}
