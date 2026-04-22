package com.auctionapp.auctionappjava.server.test;

import com.auctionapp.auctionappjava.server.config.ServerProperties;
import com.auctionapp.auctionappjava.server.db.DatabaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class test {
    static void main(String[] args) throws SQLException {
        ServerProperties properties = new ServerProperties();
        DatabaseManager.initialize(properties);
        DatabaseManager db = DatabaseManager.getInstance();
        Connection connection = db.getConnection();
        System.out.println(connection);
    }
}
