package com.hotelbooking.hotel_booking.lock;

import java.util.function.Supplier;

public interface LockManager {

    <T> T withLock(String key, Supplier<T> action);
}
