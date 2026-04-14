package com.movie.cinema_booking_backend.response;

import com.movie.cinema_booking_backend.enums.ServiceCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExtraServiceResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal unitPrice;
    private String description;
    private ServiceCategory category;
    private Boolean isActive;
}
