package com.movie.cinema_booking_backend.service.bookingticket.singleton;

import com.movie.cinema_booking_backend.exception.AppException;
import com.movie.cinema_booking_backend.exception.ErrorCode;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SeatLockRegistry {
    private SeatLockRegistry(){
    }

    // Bill Pugh singleton: thread-safe, lazy-init, no explicit synchronized block.
    private static class Holder {
        private static final SeatLockRegistry INSTANCE = new SeatLockRegistry();
    }

    public static SeatLockRegistry getInstance(){
        return Holder.INSTANCE;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException("SeatLockRegistry la Singleton");
    }
    private Object readResolve(){
        return getInstance();
    }
    // Key showtimeId:seatId - Value seatLockEntry

    private final Map<String, SeatLockEntry> lockMap = new ConcurrentHashMap<>();
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    // Cac ham business
    public void tryLock(String showtimeId, String seatId, String userId, Duration ttl){
        String key = buildKey(showtimeId, seatId);
        Duration effectiveTtl = resolveTtl(ttl);
        lockMap.compute(key, (k, existing) -> {
            if (existing != null && !existing.isExpired() && !existing.userId().equals(userId)) {
                throw new AppException(ErrorCode.SEAT_ALREADY_TAKEN);
            }
            return new SeatLockEntry(userId, Instant.now().plus(effectiveTtl));
        });
    }

    public void tryLockAll(String showtimeId, List<String> seatIds, String userId, Duration ttl){
        List<String> acquiredSeatIds = new ArrayList<>();
        try {
            for (String seatId: new LinkedHashSet<>(seatIds)){
                tryLock(showtimeId, seatId, userId, ttl);
                acquiredSeatIds.add(seatId);
            }
        } catch (AppException e){
            for (String acquiredSeatId : acquiredSeatIds){
                unlockSilently(showtimeId, acquiredSeatId, userId);
            }
            throw e;
        }
    }
    public void unlock(String showtimeId, String seatId, String userId){
        String key = buildKey(showtimeId, seatId);
        lockMap.computeIfPresent(key, (k, entry) ->
                entry.userId().equals(userId) ? null : entry
        );
    }
    public void unlockAll(String showtimeId, List<String> seatIds, String userId){
        seatIds.forEach(seatId -> unlock(showtimeId, seatId, userId));
    }

    public void unlockForce(String showtimeId, String seatId) {
        lockMap.remove(buildKey(showtimeId, seatId));
    }

    public void unlockAllForce(String showtimeId, List<String> seatIds) {
        seatIds.forEach(seatId -> unlockForce(showtimeId, seatId));
    }

    public boolean isLockedByOther(String showtimeId, String seatId, String userId){
        String key = buildKey(showtimeId, seatId);
        SeatLockEntry entry = lockMap.computeIfPresent(key, (k, current) -> current.isExpired() ? null : current);
        if (entry == null){
            return false;
        }
        return !entry.userId().equals(userId);
    }

    public boolean isLockedByUser(String showtimeId, String seatId, String userId) {
        String key = buildKey(showtimeId, seatId);
        SeatLockEntry entry = lockMap.computeIfPresent(key, (k, current) -> current.isExpired() ? null : current);
        return entry != null && entry.userId().equals(userId);
    }

    public boolean isLocked(String showtimeId, String seatId){
        String key = buildKey(showtimeId, seatId);
        SeatLockEntry entry = lockMap.computeIfPresent(key, (k, current) -> current.isExpired() ? null : current);
        return entry != null;
    }
    public void evictExpired(){
        lockMap.entrySet().removeIf(e -> e.getValue().isExpired());
    }
    //Helpers
    private String buildKey(String showtimeId, String seatId){
        return showtimeId + ":" + seatId;
    }
    private void unlockSilently(String showtimeId, String seatId, String userId){
        try {unlock(showtimeId, seatId, userId);}
        catch (Exception ignored) {}
    }

    private Duration resolveTtl(Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return DEFAULT_TTL;
        }
        return ttl;
    }
}
