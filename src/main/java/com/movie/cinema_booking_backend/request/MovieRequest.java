package com.movie.cinema_booking_backend.request;

import com.movie.cinema_booking_backend.enums.MovieStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieRequest {

    @NotBlank(message = "Tên phim không được để trống")
    @Size(max = 255, message = "Tên phim không được vượt quá 255 ký tự")
    private String title;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private String director;
    private String cast;

    @Positive(message = "Thời lượng phim phải lớn hơn 0")
    private int durationMinutes;

    @NotNull(message = "Ngày phát hành không được để trống")
    private LocalDate releaseDate;

    private String posterUrl;
    private String trailerUrl;

    @NotBlank(message = "Độ tuổi quy định không được để trống")
    private String ageRating;

    @NotNull(message = "Trạng thái phim không được để trống")
    private MovieStatus status;

    private List<String> genreIds;
}
