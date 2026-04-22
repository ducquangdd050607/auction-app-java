package com.auctionapp.auctionappjava.common.util;

import java.time.LocalDateTime;

public final class TimeUtils {

    private TimeUtils() {}

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}