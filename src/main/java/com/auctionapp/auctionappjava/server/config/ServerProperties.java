package com.auctionapp.auctionappjava.server.config;

import com.auctionapp.auctionappjava.common.config.AppConstants;

public class ServerProperties {
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final int serverPort;
    private final String adminKey;
    private final long monitorIntervalMs;

    public ServerProperties(String jdbcUrl, String jdbcUser, String jdbcPassword, int serverPort, String adminKey, long monitorIntervalMs) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcUser = jdbcUser;
        this.jdbcPassword = jdbcPassword;
        this.serverPort = serverPort;
        this.adminKey = adminKey;
        this.monitorIntervalMs = monitorIntervalMs;
    }

    public static ServerProperties load() {
        return new ServerProperties(
                pick("auction.db.url", "AUCTION_DB_URL", "jdbc:mysql://localhost:3306/auction_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh"),
                pick("auction.db.user", "AUCTION_DB_USER", "root"),
                pick("auction.db.password", "AUCTION_DB_PASSWORD", "230607"),
                parseInt(pick("auction.server.port", "AUCTION_SERVER_PORT", String.valueOf(AppConstants.DEFAULT_SERVER_PORT)), AppConstants.DEFAULT_SERVER_PORT),
                pick("auction.admin.key", "AUCTION_ADMIN_KEY", "admin-2026"),
                parseLong(pick("auction.monitor.interval.ms", "AUCTION_MONITOR_INTERVAL_MS", String.valueOf(AppConstants.DEFAULT_MONITOR_INTERVAL_MS)), AppConstants.DEFAULT_MONITOR_INTERVAL_MS));
    }

    private static String pick(String prop, String env, String fallback) {
        String value = System.getProperty(prop);
        if (value == null || value.isBlank()) value = System.getenv(env);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static int parseInt(String text, int fallback) {
        try { return Integer.parseInt(text); } catch (Exception e) { return fallback; }
    }

    private static long parseLong(String text, long fallback) {
        try { return Long.parseLong(text); } catch (Exception e) { return fallback; }
    }

    public String getJdbcUrl() { return jdbcUrl; }
    public String getJdbcUser() { return jdbcUser; }
    public String getJdbcPassword() { return jdbcPassword; }
    public int getServerPort() { return serverPort; }
    public String getAdminKey() { return adminKey; }
    public long getMonitorIntervalMs() { return monitorIntervalMs; }
}
