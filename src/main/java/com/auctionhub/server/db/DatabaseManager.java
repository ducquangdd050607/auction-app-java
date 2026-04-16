package com.auctionhub.server.db;

import com.auctionhub.server.config.ServerProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {
    private static DatabaseManager instance;
    private final ServerProperties properties;

    private DatabaseManager(ServerProperties properties) {
        this.properties = properties;
        try {
            if (properties.isMysql()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else {
                Class.forName("org.h2.Driver");
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Thiếu JDBC driver cho cơ sở dữ liệu đã cấu hình.", ex);
        }
    }

    public static synchronized DatabaseManager initialize(ServerProperties properties) {
        if (instance == null) {
            instance = new DatabaseManager(properties);
        }
        return instance;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseManager chưa được khởi tạo.");
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(properties.getDbUrl(), properties.getDbUsername(), properties.getDbPassword());
    }
}
