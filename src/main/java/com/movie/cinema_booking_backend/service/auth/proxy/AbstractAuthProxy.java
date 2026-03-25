package com.movie.cinema_booking_backend.service.auth.proxy;

import com.movie.cinema_booking_backend.service.IAuthService;

public abstract class AbstractAuthProxy implements IAuthService {

    protected final IAuthService next;

    protected AbstractAuthProxy(IAuthService next) {
        this.next = next;
    }
}
