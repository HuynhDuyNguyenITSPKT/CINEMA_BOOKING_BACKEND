package com.movie.cinema_booking_backend.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class PaginationResponse<T> {
    private List<T> currentItems;
    private int totalPages;
    private int currentPage;

    private PaginationResponse(Builder<T> builder) {
        this.currentItems = builder.currentItems;
        this.totalPages = builder.totalPages;
        this.currentPage = builder.currentPage;
    }

    public static class Builder<T> {
        private List<T> currentItems;
        private int totalPages;
        private int currentPage;

        public Builder<T> currentItems(List<T> currentItems) {
            this.currentItems = currentItems;
            return this;
        }

        public Builder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder<T> currentPage(int currentPage) {
            this.currentPage = currentPage;
            return this;
        }

        public PaginationResponse<T> build() {
            return new PaginationResponse<>(this);
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
}
