package com.movie.cinema_booking_backend.response;

import lombok.Getter;

import java.util.List;

/**
 * PaginationResponse — Wrapper phân trang chuẩn cho mọi list API.
 *
 * <p>Gồm đủ thông tin để client render UI phân trang:
 * <ul>
 *   <li>{@code currentItems} — danh sách items trang hiện tại</li>
 *   <li>{@code totalItems}   — tổng số records trong toàn bộ dataset</li>
 *   <li>{@code totalPages}   — tổng số trang dựa vào size</li>
 *   <li>{@code currentPage}  — trang hiện tại (0-indexed)</li>
 * </ul>
 *
 * <p>Design Pattern — Builder: constructor private, khởi tạo qua Builder.
 * OOP — Immutable: fields được set một lần trong constructor, không có setter.
 */
@Getter
public class PaginationResponse<T> {

    private final List<T> currentItems;
    private final long totalItems;
    private final int totalPages;
    private final int currentPage;

    private PaginationResponse(Builder<T> builder) {
        this.currentItems = builder.currentItems;
        this.totalItems   = builder.totalItems;
        this.totalPages   = builder.totalPages;
        this.currentPage  = builder.currentPage;
    }

    public static class Builder<T> {
        private List<T> currentItems;
        private long totalItems;
        private int totalPages;
        private int currentPage;

        public Builder<T> currentItems(List<T> currentItems) {
            this.currentItems = currentItems;
            return this;
        }

        public Builder<T> totalItems(long totalItems) {
            this.totalItems = totalItems;
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
