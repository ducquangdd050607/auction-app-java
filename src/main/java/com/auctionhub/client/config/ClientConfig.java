package com.auctionhub.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfig {
    private final Properties properties = new Properties();

    public ClientConfig() {
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
        return Integer.parseInt(properties.getProperty("app.server.port", "9090"));
    }
}
