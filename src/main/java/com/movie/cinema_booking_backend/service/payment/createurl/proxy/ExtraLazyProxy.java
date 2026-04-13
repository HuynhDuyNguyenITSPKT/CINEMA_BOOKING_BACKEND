package com.movie.cinema_booking_backend.service.payment.createurl.proxy;

import com.movie.cinema_booking_backend.enums.ServiceCategory;
import com.movie.cinema_booking_backend.request.ExtraServiceRequest;
import com.movie.cinema_booking_backend.response.ExtraServiceResponse;
import com.movie.cinema_booking_backend.service.IExtraServiceService;
import com.movie.cinema_booking_backend.service.impl.ExtraServiceService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExtraLazyProxy implements IExtraServiceService {

	private static final long CACHE_TTL_MILLIS = 60_000L;

	private final ExtraServiceService delegate;
	private final Map<String, CacheEntry<Page<ExtraServiceResponse>>> adminListCache = new ConcurrentHashMap<>();

	public ExtraLazyProxy(ExtraServiceService delegate) {
		this.delegate = delegate;
	}

	@Override
	// Delegate trực tiếp cho luồng user (không cache).
	public Page<ExtraServiceResponse> getUserExtraServices(int page, int size, ServiceCategory category) {
		return delegate.getUserExtraServices(page, size, category);
	}

	@Override
	// Danh sách admin có cache TTL để giảm tải DB ở truy vấn lặp.
	public Page<ExtraServiceResponse> getAllExtraServicesForAdmin(int page, int size, Boolean isActive, ServiceCategory category) {
		String cacheKey = buildAdminListCacheKey(page, size, isActive, category);
		CacheEntry<Page<ExtraServiceResponse>> cached = adminListCache.get(cacheKey);

		if (cached != null && !cached.isExpired()) {
			System.out.println("Lấy từ cache cho key: " + cacheKey);
			return cached.value();
		}
		System.out.println("lấy dữ liệu mới từ delegate");
		Page<ExtraServiceResponse> freshData = delegate.getAllExtraServicesForAdmin(page, size, isActive, category);
		adminListCache.put(cacheKey, new CacheEntry<>(freshData, System.currentTimeMillis() + CACHE_TTL_MILLIS));
		return freshData;
	}

	@Override
	// Lấy chi tiết 1 dịch vụ thêm.
	public ExtraServiceResponse getExtraServiceById(Long id) {
		return delegate.getExtraServiceById(id);
	}

	@Override
	// Tạo mới rồi clear cache list admin để tránh dữ liệu cũ.
	public ExtraServiceResponse createExtraService(ExtraServiceRequest request) {
		ExtraServiceResponse created = delegate.createExtraService(request);
		clearAdminListCache();
		return created;
	}

	@Override
	// Cập nhật rồi clear cache list admin.
	public ExtraServiceResponse updateExtraService(Long id, ExtraServiceRequest request) {
		ExtraServiceResponse updated = delegate.updateExtraService(id, request);
		clearAdminListCache();
		return updated;
	}

	@Override
	// Xóa rồi clear cache list admin.
	public void deleteExtraService(Long id) {
		delegate.deleteExtraService(id);
		clearAdminListCache();
	}

	// Build key cache theo đầy đủ tiêu chí phân trang/lọc.
	private String buildAdminListCacheKey(int page, int size, Boolean isActive, ServiceCategory category) {
		return page + ":" + size + ":" + Objects.toString(isActive, "null") + ":" + Objects.toString(category, "null");
	}

	// Invalidate toàn bộ cache danh sách admin.
	private void clearAdminListCache() {
		adminListCache.clear();
	}

	private record CacheEntry<T>(T value, long expireAt) {
		// Kiểm tra entry đã quá hạn chưa.
		boolean isExpired() {
			return System.currentTimeMillis() > expireAt;
		}
	}
}
