package com.auctionhub.server.config;

import com.auctionhub.common.config.AppConstants;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

public class ServerProperties {
    private final Properties properties = new Properties();

    public ServerProperties() {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể đọc application.properties", ex);
        }
    }

    public String getHost() {
        return properties.getProperty("app.server.host", "127.0.0.1");
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("app.server.port", String.valueOf(AppConstants.DEFAULT_SERVER_PORT)));
    }

    public int getBacklog() {
        return Integer.parseInt(properties.getProperty("app.server.backlog", "100"));
    }

    public int getAcceptTimeoutMs() {
        return Integer.parseInt(properties.getProperty("app.server.accept-timeout-ms", "500"));
    }

    public BigDecimal getMinimumIncrement() {
        return new BigDecimal(properties.getProperty("app.auction.minimum-increment", AppConstants.DEFAULT_MIN_INCREMENT.toPlainString()));
    }

    public long getMonitorIntervalMs() {
        return Long.parseLong(properties.getProperty("app.auction.monitor-interval-ms", String.valueOf(AppConstants.DEFAULT_MONITOR_INTERVAL_MS)));
    }

    public long getAntiSnipingThresholdSeconds() {
        return Long.parseLong(properties.getProperty("app.auction.anti-sniping-threshold-seconds", String.valueOf(AppConstants.DEFAULT_ANTI_SNIPING_THRESHOLD_SECONDS)));
    }

    public long getAntiSnipingExtensionSeconds() {
        return Long.parseLong(properties.getProperty("app.auction.anti-sniping-extension-seconds", String.valueOf(AppConstants.DEFAULT_ANTI_SNIPING_EXTENSION_SECONDS)));
    }

    public String getDbVendor() {
        return properties.getProperty("app.db.vendor", "h2");
    }

    public String getDbUrl() {
        return isMysql() ? properties.getProperty("app.db.mysql.url") : properties.getProperty("app.db.url");
    }

    public String getDbUsername() {
        return isMysql() ? properties.getProperty("app.db.mysql.username") : properties.getProperty("app.db.username");
    }

    public String getDbPassword() {
        return isMysql() ? properties.getProperty("app.db.mysql.password") : properties.getProperty("app.db.password");
    }

    public boolean isMysql() {
        return "mysql".equalsIgnoreCase(getDbVendor());
    }
}
