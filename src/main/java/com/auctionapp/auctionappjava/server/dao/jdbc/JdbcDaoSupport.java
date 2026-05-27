package com.auctionapp.auctionappjava.server.dao.jdbc;

import com.auctionapp.auctionappjava.server.dao.db.DatabaseManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

abstract class JdbcDaoSupport {
  protected Connection connection() throws SQLException {
    return DatabaseManager.getInstance().getConnection();
  }

  protected String uuid(UUID value) {
    return value == null ? null : value.toString();
  }

  protected UUID uuid(String value) {
    return value == null ? null : UUID.fromString(value);
  }

  protected Timestamp timestamp(LocalDateTime value) {
    return value == null ? null : Timestamp.valueOf(value);
  }

  protected LocalDateTime localDateTime(Timestamp value) {
    return value == null ? null : value.toLocalDateTime();
  }
}
