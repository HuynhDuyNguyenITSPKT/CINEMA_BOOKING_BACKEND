package com.movie.cinema_booking_backend.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    private ApiResponse(Builder<T> builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
    }

    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<>(this);
        }
    }
}
