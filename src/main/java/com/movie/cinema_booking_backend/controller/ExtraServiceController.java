package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.enums.ServiceCategory;
import com.movie.cinema_booking_backend.request.ExtraServiceRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.ExtraServiceResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.service.IExtraServiceService;
import com.movie.cinema_booking_backend.service.payment.createurl.proxy.ExtraLazyProxy;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class ExtraServiceController {

    private final IExtraServiceService extraServiceService;

    public ExtraServiceController(ExtraLazyProxy extraServiceService) {
        this.extraServiceService = extraServiceService;
    }

    @GetMapping("/extra-services")
    public ApiResponse<?> getUserExtraServices(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) ServiceCategory category) {
        var pageResult = extraServiceService.getUserExtraServices(page, size, category);
        var pagination = new PaginationResponse.Builder<ExtraServiceResponse>()
                .currentItems(pageResult.getContent())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy danh sách dịch vụ thêm thành công")
                .data(pagination)
                .build();
    }

    @GetMapping("/admin/extra-services")
    public ApiResponse<?> getAllExtraServicesForAdmin(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) Boolean isActive,
                                                      @RequestParam(required = false) ServiceCategory category) {
        var pageResult = extraServiceService.getAllExtraServicesForAdmin(page, size, isActive, category);
        var pagination = new PaginationResponse.Builder<ExtraServiceResponse>()
                .currentItems(pageResult.getContent())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy danh sách dịch vụ thêm thành công")
                .data(pagination)
                .build();
    }

    @GetMapping("extra-services/{id}")
    public ApiResponse<?> getExtraServiceById(@PathVariable Long id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy chi tiết dịch vụ thêm thành công")
                .data(extraServiceService.getExtraServiceById(id))
                .build();
    }

    @PostMapping("/admin/extra-services")
    public ApiResponse<?> createExtraService(@Valid @RequestBody ExtraServiceRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Tạo dịch vụ thêm thành công")
                .data(extraServiceService.createExtraService(request))
                .build();
    }

    @PutMapping("/admin/extra-services/{id}")
    public ApiResponse<?> updateExtraService(@PathVariable Long id, @Valid @RequestBody ExtraServiceRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Cập nhật dịch vụ thêm thành công")
                .data(extraServiceService.updateExtraService(id, request))
                .build();
    }

    @DeleteMapping("/admin/extra-services/{id}")
    public ApiResponse<?> deleteExtraService(@PathVariable Long id) {
        extraServiceService.deleteExtraService(id);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Xóa dịch vụ thêm thành công")
                .build();
    }
}
