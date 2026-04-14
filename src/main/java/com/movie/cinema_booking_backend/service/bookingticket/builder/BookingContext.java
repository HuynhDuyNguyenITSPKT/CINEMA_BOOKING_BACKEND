package com.movie.cinema_booking_backend.service.bookingticket.builder;

import com.movie.cinema_booking_backend.entity.ExtraService;
import com.movie.cinema_booking_backend.entity.Promotion;
import com.movie.cinema_booking_backend.entity.Seat;
import com.movie.cinema_booking_backend.entity.Showtime;
import com.movie.cinema_booking_backend.entity.User;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════
 *  DATA TRANSFER OBJECT: BOOKING CONTEXT
 * ═══════════════════════════════════════════════════════════
 * 
 * Class này đóng vai trò là "rổ nguyên liệu" được chuẩn bị bởi Service Layer.
 * Service Layer sẽ query DB, rải DB Lock (nếu cần), kiểm tra DB Guard,
 * rồi gom hết entity vào đây để ném cho Builder.
 * 
 * Nhờ có class này, Builder không cần phải dính dáng đến bắt cứ Repository (DB) nào.
 */
public record BookingContext(
        Showtime showtime,
        User user,
        List<Seat> seats,
        Promotion promotion,
        List<ExtraService> extraServices
) {}
