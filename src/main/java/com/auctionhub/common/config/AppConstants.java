package com.auctionhub.common.config;

import java.math.BigDecimal;

public final class AppConstants {
    public static final BigDecimal DEFAULT_MIN_INCREMENT = BigDecimal.valueOf(10);
    public static final int DEFAULT_SERVER_PORT = 9090;
    public static final long DEFAULT_MONITOR_INTERVAL_MS = 1000L;
    public static final long DEFAULT_ANTI_SNIPING_THRESHOLD_SECONDS = 30L;
    public static final long DEFAULT_ANTI_SNIPING_EXTENSION_SECONDS = 60L;

    private AppConstants() {
    }
}
