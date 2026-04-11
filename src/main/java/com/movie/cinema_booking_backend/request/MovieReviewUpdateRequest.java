package com.movie.cinema_booking_backend.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieReviewUpdateRequest {

    @NotNull(message = "Điểm đánh giá không được để trống")
    @DecimalMin(value = "0.0", message = "Điểm đánh giá phải từ 0 đến 5")
    @DecimalMax(value = "5.0", message = "Điểm đánh giá phải từ 0 đến 5")
    private Double rating;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(max = 2000, message = "Nội dung bình luận không được vượt quá 2000 ký tự")
    private String comment;
}
