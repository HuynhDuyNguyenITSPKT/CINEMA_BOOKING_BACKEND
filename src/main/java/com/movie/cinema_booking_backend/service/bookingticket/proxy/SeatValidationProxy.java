package com.movie.cinema_booking_backend.service.bookingticket.proxy;

import com.movie.cinema_booking_backend.response.SeatResponse;
import com.movie.cinema_booking_backend.service.ISeatService;
import com.movie.cinema_booking_backend.service.bookingticket.singleton.SeatLockRegistry;
import com.movie.cinema_booking_backend.service.impl.SeatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════
 * DESIGN PATTERN: PROXY (GoF Classic Implementation)
 * ═══════════════════════════════════════════════════════════
 *
 * Subject Interface : ISeatService
 * Real Subject : SeatServiceImpl
 * Proxy : SeatValidationProxy (this class, @Primary)
 *
 * Proxy types combined:
 * • Virtual Proxy : Cache seat-map results (5s TTL)
 * • Smart Proxy : Enrich with real-time lock status from RAM
 */
@Primary
@Service
@RequiredArgsConstructor
public class SeatValidationProxy implements ISeatService {
    private final SeatServiceImpl realSeatService;
    private final SeatLockRegistry lockRegistry = SeatLockRegistry.getInstance();
    private final Map<String, CacheEntry<List<SeatResponse>>> seatMapCache = new ConcurrentHashMap<>();
    private static final long SEAT_MAP_CACHE_TTL_MILLIS = 5_000L;

    @Override
    public List<SeatResponse> getSeatsByAuditorium(String auditoriumId) {
        return realSeatService.getSeatsByAuditorium(auditoriumId);
    }

    @Override
    public List<SeatResponse> getSeatMapByShowtime(String showtimeId, String currentUserId) {
        // 1. Check cache
        String cacheKey = buildCacheKey(showtimeId, currentUserId);
        List<SeatResponse> cached = getCachedSeatMap(cacheKey);

        if (cached != null) {
            // Cache hit → enrich with lock status and return
            return enrichWithLockStatus(cached, showtimeId, currentUserId);
        }

        // 2. Cache miss → delegate to RealSubject
        List<SeatResponse> seats = realSeatService.getSeatMapByShowtime(showtimeId, currentUserId);

        // 3. Update cache
        seatMapCache.put(cacheKey, new CacheEntry<>(seats, System.currentTimeMillis() + SEAT_MAP_CACHE_TTL_MILLIS));

        // 4. Enrich with real-time lock status (Smart Proxy)
        return enrichWithLockStatus(seats, showtimeId, currentUserId);
    }

    private String buildCacheKey(String showtimeId, String userId) {
        return showtimeId + ":" + (userId != null ? userId : "anonymous");
    }

    private List<SeatResponse> getCachedSeatMap(String cacheKey) {
        CacheEntry<List<SeatResponse>> entry = seatMapCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            System.out.println("[Proxy] Cache HIT: " + cacheKey);
            return entry.value();
        }
        System.out.println("[Proxy] Cache MISS: " + cacheKey);
        return null;
    }

    private List<SeatResponse> enrichWithLockStatus(List<SeatResponse> seats,
            String showtimeId,
            String currentUserId) {
        return seats.stream()
                .map(seat -> {
                    // BOOKED from DB takes priority
                    if ("BOOKED".equals(seat.getStatus())) {
                        return seat;
                    }
                    // Check if locked by another user (real-time RAM check)
                    if (lockRegistry.isLockedByOther(showtimeId, seat.getId(), currentUserId)) {
                        return SeatResponse.builder()
                                .id(seat.getId())
                                .name(seat.getName())
                                .rowIndex(seat.getRowIndex())
                                .columnIndex(seat.getColumnIndex())
                                .seatTypeId(seat.getSeatTypeId())
                                .seatTypeName(seat.getSeatTypeName())
                                .seatTypeSurcharge(seat.getSeatTypeSurcharge())
                                .status("LOCKED") // ✅ Proxy adds this status
                                .build();
                    }
                    return seat;
                })
                .collect(Collectors.toList());
    }

    private record CacheEntry<T>(T value, long expireAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }
}
