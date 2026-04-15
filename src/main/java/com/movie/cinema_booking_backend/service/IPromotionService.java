package com.movie.cinema_booking_backend.service;

import com.movie.cinema_booking_backend.entity.Promotion;
import com.movie.cinema_booking_backend.request.PromotionRequest;
import com.movie.cinema_booking_backend.response.PromotionResponse;
import org.springframework.data.domain.Page;

public interface IPromotionService {
    Page<PromotionResponse> getAllPromotions(int page, int size, Boolean isActive);

    PromotionResponse getPromotionById(String id);

    PromotionResponse createPromotion(PromotionRequest request);

    PromotionResponse updatePromotion(String id, PromotionRequest request);

    void deletePromotion(String id);

    PromotionResponse getPromotionByCode(String Code);

    boolean decrementUsage(String code);
}
