package com.movie.cinema_booking_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface IImageUploadService {
    String uploadImage(MultipartFile file);
}