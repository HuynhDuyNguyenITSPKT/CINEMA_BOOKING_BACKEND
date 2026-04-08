package com.movie.cinema_booking_backend.service.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.movie.cinema_booking_backend.response.AdminUserAccountResponse;

@Component
public class UserAdminPageCache {

    private final Map<String, Page<AdminUserAccountResponse>> cache = new ConcurrentHashMap<>();

    public Page<AdminUserAccountResponse> get(String key) {
        return cache.get(key);
    }

    public void put(String key, Page<AdminUserAccountResponse> value) {
        cache.put(key, value);
    }

    public void clear() {
        cache.clear();
    }
}