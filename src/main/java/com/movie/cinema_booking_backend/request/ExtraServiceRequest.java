package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.ServiceCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExtraServiceRequest {
    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String name;

    @NotBlank(message = "Ảnh dịch vụ không được để trống")
    private String imageUrl;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    private String description;

    @NotNull(message = "Loại dịch vụ không được để trống")
    private ServiceCategory category;

    private Boolean isActive;
}
