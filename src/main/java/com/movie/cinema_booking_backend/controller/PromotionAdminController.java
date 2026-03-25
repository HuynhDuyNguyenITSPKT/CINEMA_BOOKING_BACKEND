package com.movie.cinema_booking_backend.controller;

import com.movie.cinema_booking_backend.request.PromotionRequest;
import com.movie.cinema_booking_backend.response.ApiResponse;
import com.movie.cinema_booking_backend.response.PaginationResponse;
import com.movie.cinema_booking_backend.response.PromotionResponse;
import com.movie.cinema_booking_backend.service.IPromotionService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/promotions")
@Validated
public class PromotionAdminController {

    private final IPromotionService promotionService;

    public PromotionAdminController(IPromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping("")
    public ApiResponse<?> getAllPromotions(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Boolean isActive) {
        var pageResult = promotionService.getAllPromotions(page, size, isActive);
        var pagination = new PaginationResponse.Builder<PromotionResponse>()
                .currentItems(pageResult.getContent())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getNumber())
                .build();

        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy danh sách khuyến mãi thành công")
                .data(pagination)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getPromotionById(@PathVariable String id) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Lấy thông tin khuyến mãi thành công")
                .data(promotionService.getPromotionById(id))
                .build();
    }

    @PostMapping("")
    public ApiResponse<?> createPromotion(@Valid @RequestBody PromotionRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Tạo khuyến mãi thành công")
                .data(promotionService.createPromotion(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updatePromotion(@PathVariable String id, @Valid @RequestBody PromotionRequest request) {
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Cập nhật khuyến mãi thành công")
                .data(promotionService.updatePromotion(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePromotion(@PathVariable String id) {
        promotionService.deletePromotion(id);
        return new ApiResponse.Builder<>()
                .success(true)
                .message("Xóa khuyến mãi thành công")
                .build();
    }
}
