package com.auctionapp.auctionappjava.server.db;

import com.auctionapp.auctionappjava.server.config.ServerProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final ServerProperties properties;

    public DatabaseManager() {
        this(ServerProperties.load());
    }

    public DatabaseManager(ServerProperties properties) {
        this.properties = properties;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(properties.getJdbcUrl(), properties.getJdbcUser(), properties.getJdbcPassword());
    }

    public ServerProperties getProperties() { return properties; }
}
