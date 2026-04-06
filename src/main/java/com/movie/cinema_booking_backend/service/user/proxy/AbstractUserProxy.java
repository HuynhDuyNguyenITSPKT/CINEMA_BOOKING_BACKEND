package com.movie.cinema_booking_backend.service.user.proxy;

import com.movie.cinema_booking_backend.service.IUserService;

public abstract class AbstractUserProxy implements IUserService {

    protected final IUserService next;

    protected AbstractUserProxy(IUserService next) {
        this.next = next;
    }

}