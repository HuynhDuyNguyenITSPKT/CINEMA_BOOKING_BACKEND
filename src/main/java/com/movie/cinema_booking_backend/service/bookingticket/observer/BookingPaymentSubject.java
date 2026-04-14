package com.movie.cinema_booking_backend.service.bookingticket.observer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ════════════════════════════════════════════════════════════
 *  DESIGN PATTERN: OBSERVER — ConcreteSubject (GoF)
 * ════════════════════════════════════════════════════════════
 * Nắm giữ State danh sách Observer và kích hoạt Notification.
 */
@Component
public class BookingPaymentSubject implements IBookingSubject {

    private final List<IBookingObserver> observers = new ArrayList<>();
    private BookingSuccessEvent state;

    // Spring tự động tiêm tất cả các Beans implements IBookingObserver vào đây.
    public BookingPaymentSubject(List<IBookingObserver> observers) {
        if (observers != null) {
            observers.forEach(this::registerObserver);
        }
    }

    @Override
    public void registerObserver(IBookingObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(IBookingObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (IBookingObserver observer : observers) {
            observer.update(this); // PULL MODEL: Pass `this`
        }
    }

    /**
     * Bị gọi bởi PaymentFacade (thông qua BookingFacade).
     * Khi hàm này kích hoạt, State thay đổi và tự động báo cho Observers.
     */
    public void setPaymentSuccessState(BookingSuccessEvent state) {
        this.state = state;
        notifyObservers();
    }

    /**
     * Hàm dành riêng cho Observers PULL data ra ngoài.
     */
    public BookingSuccessEvent getState() {
        return state;
    }
}
