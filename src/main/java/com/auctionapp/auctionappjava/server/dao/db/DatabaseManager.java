package com.auctionapp.auctionappjava.server.dao.db;

import com.auctionapp.auctionappjava.server.config.ServerProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseManager {
  private static final DatabaseManager INSTANCE = new DatabaseManager();
  private final HikariDataSource dataSource;

  private DatabaseManager() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(ServerProperties.DB_URL);
    config.setUsername(ServerProperties.DB_USER);
    config.setPassword(ServerProperties.DB_PASSWORD);
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    config.setPoolName("auction-db-pool");
    config.setMaximumPoolSize(Integer.getInteger("auction.db.pool.maxSize", 10));
    config.setMinimumIdle(Integer.getInteger("auction.db.pool.minIdle", 2));
    config.setConnectionTimeout(Long.getLong("auction.db.pool.connectionTimeoutMs", 10_000L));
    config.setIdleTimeout(Long.getLong("auction.db.pool.idleTimeoutMs", 300_000L));
    config.setMaxLifetime(Long.getLong("auction.db.pool.maxLifetimeMs", 1_500_000L));
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    this.dataSource = new HikariDataSource(config);
  }

  public static DatabaseManager getInstance() {
    return INSTANCE;
  }

  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  public void shutdown() {
    dataSource.close();
  }
}
