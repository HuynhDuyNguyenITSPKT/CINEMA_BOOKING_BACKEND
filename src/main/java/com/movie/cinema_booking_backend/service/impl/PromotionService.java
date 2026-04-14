package com.movie.cinema_booking_backend.service.impl;

import com.movie.cinema_booking_backend.entity.Promotion;
import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import com.movie.cinema_booking_backend.repository.PromotionRepository;
import com.movie.cinema_booking_backend.request.PromotionRequest;
import com.movie.cinema_booking_backend.response.PromotionResponse;
import com.movie.cinema_booking_backend.service.IPromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PromotionService implements IPromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }
    
    @Override
    public PromotionResponse getPromotionByCode(String code) {
        Promotion promotion = promotionRepository.getPromotionByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        return toResponse(promotion);
    }

    @Override
    public Page<PromotionResponse> getAllPromotions(int page, int size, Boolean isActive) {
        var pageable = PageRequest.of(page, size);
        var promotionPage = isActive == null
                ? promotionRepository.findAll(pageable)
                : promotionRepository.findByIsActive(isActive, pageable);

        return promotionPage.map(this::toResponse);
    }

    @Override
    public PromotionResponse getPromotionById(String id) {
        Promotion promotion = findByIdOrThrow(id);
        return toResponse(promotion);
    }

    @Override
    public PromotionResponse createPromotion(PromotionRequest request) {
        if (promotionRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.PROMOTION_CODE_EXISTS);
        }

        validateDateRange(request);

        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(defaultBigDecimal(request.getDiscountValue()))
                .maxDiscountAmount(defaultBigDecimal(request.getMaxDiscountAmount()))
                .minTicketRequired(defaultMinTicket(request.getMinTicketRequired()))
                .minOrderValue(defaultBigDecimal(request.getMinOrderValue()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .code(request.getCode())
                .quantity(request.getQuantity())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .imageUrl(request.getImageUrl())
                .build();

        return toResponse(promotionRepository.save(promotion));
    }

    @Override
    public PromotionResponse updatePromotion(String id, PromotionRequest request) {
        Promotion promotion = findByIdOrThrow(id);

        if (promotionRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new AppException(ErrorCode.PROMOTION_CODE_EXISTS);
        }

        validateDateRange(request);

        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(defaultBigDecimal(request.getDiscountValue()));
        promotion.setMaxDiscountAmount(defaultBigDecimal(request.getMaxDiscountAmount()));
        promotion.setMinTicketRequired(defaultMinTicket(request.getMinTicketRequired()));
        promotion.setMinOrderValue(defaultBigDecimal(request.getMinOrderValue()));
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setCode(request.getCode());
        promotion.setQuantity(request.getQuantity());
        promotion.setActive(request.getIsActive() != null ? request.getIsActive() : promotion.isActive());
        promotion.setImageUrl(request.getImageUrl());

        return toResponse(promotionRepository.save(promotion));
    }

    @Override
    public void deletePromotion(String id) {
        Promotion promotion = findByIdOrThrow(id);
        promotionRepository.delete(promotion);
    }

    @Override
    @Transactional
    public boolean decrementUsage(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return promotionRepository.decrementQuantityIfAvailable(code.trim()) > 0;
    }

    private Promotion findByIdOrThrow(String id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
    }

    private void validateDateRange(PromotionRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.INVALID_PROMOTION_DATE_RANGE);
        }
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Integer defaultMinTicket(Integer value) {
        return value != null ? value : 1;
    }

    private PromotionResponse toResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .maxDiscountAmount(promotion.getMaxDiscountAmount())
                .minTicketRequired(promotion.getMinTicketRequired())
                .minOrderValue(promotion.getMinOrderValue())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .code(promotion.getCode())
                .quantity(promotion.getQuantity())
                .isActive(promotion.isActive())
                .imageUrl(promotion.getImageUrl())
                .build();
    }
}
