package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {
    @NotBlank(message = "Promotion name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount value must be greater than or equal to 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", inclusive = true, message = "Max discount amount must be greater than or equal to 0")
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Min ticket required must be at least 1")
    private Integer minTicketRequired;

    @DecimalMin(value = "0.0", inclusive = true, message = "Min order value must be greater than or equal to 0")
    private BigDecimal minOrderValue;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotBlank(message = "Code is required")
    private String code;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;

    private Boolean isActive;

    private String imageUrl;
}
