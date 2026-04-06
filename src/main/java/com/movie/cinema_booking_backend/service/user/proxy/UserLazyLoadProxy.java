package com.movie.cinema_booking_backend.service.user.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.movie.cinema_booking_backend.request.AdminAccountUpdateRequest;
import com.movie.cinema_booking_backend.request.UpdateProfileRequest;
import com.movie.cinema_booking_backend.response.AdminUserAccountResponse;
import com.movie.cinema_booking_backend.response.UserResponse;
import com.movie.cinema_booking_backend.service.impl.UserService;

import org.springframework.security.core.Authentication;

@Service("userLazyLoadProxy")
public class UserLazyLoadProxy extends AbstractUserProxy {

    private static final Logger log = LoggerFactory.getLogger(UserLazyLoadProxy.class);

    private final Map<String, Page<AdminUserAccountResponse>> adminPageCache = new ConcurrentHashMap<>();

    public UserLazyLoadProxy(UserService realService) {
        super(realService);
    }

    @Override
    public UserResponse updateProfile(Authentication authentication, UpdateProfileRequest request) {
        UserResponse updated = next.updateProfile(authentication, request);
        invalidateCaches();
        return updated;
    }

    @Override
    public Page<AdminUserAccountResponse> getUsersForAdmin(int page, int size, String keyword) {
        String cacheKey = toPageCacheKey(page, size, keyword);
        log.info("Cache Key: " + cacheKey);
        Page<AdminUserAccountResponse> cached = adminPageCache.get(cacheKey);
        if (cached != null) {
            log.info("[UserLazyLoadProxy] Lấy danh sách phân trang từ cache, key={}", cacheKey);
            return cached;
        }

        Page<AdminUserAccountResponse> loaded = next.getUsersForAdmin(page, size, keyword);
        adminPageCache.put(cacheKey, loaded);
        log.info("[UserLazyLoadProxy] Cache rỗng cho key={}, đã tải dữ liệu phân trang", cacheKey);
        return loaded;
    }

    @Override
    public AdminUserAccountResponse updateUserAccountByAdmin(Long userId, AdminAccountUpdateRequest request) {
        AdminUserAccountResponse updated = next.updateUserAccountByAdmin(userId, request);
        invalidateCaches();
        return updated;
    }

    private String toPageCacheKey(int page, int size, String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        return page + "|" + size + "|" + normalizedKeyword;
    }

    private void invalidateCaches() {
        adminPageCache.clear();
        log.info("[UserLazyLoadProxy] Đã xóa cache phân trang người dùng");
    }
}