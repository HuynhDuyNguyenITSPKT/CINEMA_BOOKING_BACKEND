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
        String cacheKey = buildCacheKey(showtimeId, currentUserId);
        List<SeatResponse> cached = getCachedSeatMap(cacheKey);

        if (cached != null) {
            return enrichWithLockStatus(cached, showtimeId, currentUserId);
        }

        List<SeatResponse> seats = realSeatService.getSeatMapByShowtime(showtimeId, currentUserId);

        seatMapCache.put(cacheKey, new CacheEntry<>(seats, System.currentTimeMillis() + SEAT_MAP_CACHE_TTL_MILLIS));

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
                    if ("BOOKED".equals(seat.getStatus())) {
                        return seat;
                    }
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
