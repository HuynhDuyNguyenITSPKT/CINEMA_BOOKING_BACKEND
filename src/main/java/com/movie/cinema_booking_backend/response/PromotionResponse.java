package com.movie.cinema_booking_backend.response;

import com.movie.cinema_booking_backend.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PromotionResponse {
    private String id;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private Integer minTicketRequired;
    private BigDecimal minOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String code;
    private Integer quantity;
    private boolean isActive;
    private String imageUrl;
}
