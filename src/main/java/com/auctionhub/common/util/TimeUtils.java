package com.auctionhub.common.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtils {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private TimeUtils() {
    }

    public static String formatDisplay(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DISPLAY_FORMAT.format(dateTime);
    }

    public static long secondsRemaining(LocalDateTime endTime, LocalDateTime now) {
        return Math.max(0, Duration.between(now, endTime).getSeconds());
    }
}
