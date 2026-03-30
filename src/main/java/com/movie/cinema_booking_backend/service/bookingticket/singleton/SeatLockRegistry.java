package com.movie.cinema_booking_backend.service.bookingticket.singleton;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ═══════════════════════════════════════════════════════════
 *  DESIGN PATTERN: SINGLETON
 * ═══════════════════════════════════════════════════════════
 *
 * Tại sao là Singleton thực sự?
 *   Spring @Component mặc định = singleton scope.
 *   Bean này giữ lockMap trong RAM — nếu có 2 instance, mỗi instance
 *   có lockMap riêng → race condition, user A thấy ghế AVAILABLE
 *   dù user B đang lock. Một instance duy nhất là điều bắt buộc.
 *
 * Tại sao ConcurrentHashMap?
 *   Nhiều HTTP request đến song song (mỗi request = 1 thread).
 *   ConcurrentHashMap thread-safe không cần synchronized block.
 *   Dùng HashMap thường → ConcurrentModificationException.
 *
 * Key format: "{showtimeId}:{seatId}"
 *   Ghế A5 có thể thuộc nhiều suất chiếu khác nhau.
 *   Lock phải scoped theo showtime, không chỉ theo seat.
 */
@Component
public class SeatLockRegistry {

    /** Key = "{showtimeId}:{seatId}", Value = SeatLockEntry */
    private final Map<String, SeatLockEntry> lockMap = new ConcurrentHashMap<>();

    /** TTL mặc định cho lock ghế: 10 phút */
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    // ─── Public API ─────────────────────────────────────────────────────────

    /**
     * Thử lock một ghế. Nếu ghế đang bị lock bởi user khác → throw SEAT_ALREADY_TAKEN.
     * Nếu chính user đó đang lock lại → gia hạn TTL (idempotent).
     */
    public void tryLock(String showtimeId, String seatId, String userId, Duration ttl) {
        String key = buildKey(showtimeId, seatId);
        SeatLockEntry existing = lockMap.get(key);

        if (existing != null && !existing.isExpired()) {
            if (!existing.userId().equals(userId)) {
                // Ghế đang bị lock bởi user khác
                throw new AppException(ErrorCode.SEAT_ALREADY_TAKEN);
            }
            // Chính user này lock lại → gia hạn (fall through để overwrite)
        }

        lockMap.put(key, new SeatLockEntry(userId, Instant.now().plus(ttl)));
    }

    /**
     * Lock nhiều ghế cùng lúc (atomic all-or-nothing).
     * Nếu bất kỳ ghế nào fail → unlock tất cả ghế đã lock trong batch này + throw.
     */
    public void tryLockAll(String showtimeId, List<String> seatIds, String userId, Duration ttl) {
        // Phase 1: thử lock từng ghế
        int lockedCount = 0;
        try {
            for (String seatId : seatIds) {
                tryLock(showtimeId, seatId, userId, ttl);
                lockedCount++;
            }
        } catch (AppException e) {
            // Rollback: unlock các ghế đã lock trong batch này
            for (int i = 0; i < lockedCount; i++) {
                unlockSilently(showtimeId, seatIds.get(i), userId);
            }
            throw e; // re-throw lỗi SEAT_ALREADY_TAKEN
        }
    }

    /**
     * Unlock một ghế (chỉ nếu chính user đó đang lock).
     * Nếu ghế không bị lock hoặc bị lock bởi user khác → no-op (idempotent).
     */
    public void unlock(String showtimeId, String seatId, String userId) {
        String key = buildKey(showtimeId, seatId);
        lockMap.computeIfPresent(key, (k, entry) ->
                entry.userId().equals(userId) ? null : entry
        );
    }

    /** Unlock nhiều ghế. */
    public void unlockAll(String showtimeId, List<String> seatIds, String userId) {
        seatIds.forEach(seatId -> unlock(showtimeId, seatId, userId));
    }

    /**
     * Kiểm tra ghế có đang bị lock bởi user KHÁC không.
     * Nếu lock đã expired → coi như không lock.
     */
    public boolean isLockedByOther(String showtimeId, String seatId, String userId) {
        SeatLockEntry entry = lockMap.get(buildKey(showtimeId, seatId));
        if (entry == null || entry.isExpired()) return false;
        return !entry.userId().equals(userId);
    }

    /**
     * Kiểm tra ghế có đang bị lock (bởi bất kỳ ai) không.
     */
    public boolean isLocked(String showtimeId, String seatId) {
        SeatLockEntry entry = lockMap.get(buildKey(showtimeId, seatId));
        return entry != null && !entry.isExpired();
    }

    // ─── Scheduled eviction ─────────────────────────────────────────────────

    /**
     * Tự dọn dẹp các lock hết hạn mỗi 30 giây.
     * entrySet().removeIf() thread-safe với ConcurrentHashMap.
     */
    @Scheduled(fixedDelay = 30_000)
    public void evictExpired() {
        lockMap.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private String buildKey(String showtimeId, String seatId) {
        return showtimeId + ":" + seatId;
    }

    private void unlockSilently(String showtimeId, String seatId, String userId) {
        try { unlock(showtimeId, seatId, userId); } catch (Exception ignored) {}
    }
}
