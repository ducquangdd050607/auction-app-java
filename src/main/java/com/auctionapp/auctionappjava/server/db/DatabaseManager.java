package com.auctionapp.auctionappjava.server.db;

import com.auctionapp.auctionappjava.server.config.ServerProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {
    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                ServerProperties.DB_URL, ServerProperties.DB_USER, ServerProperties.DB_PASSWORD);
    }
}
