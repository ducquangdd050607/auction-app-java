package com.auctionapp.auctionappjava.server.dao.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auctionapp.auctionappjava.server.dao.db.DatabaseManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class DatabaseManagerIntegrationTest {

  @Test
  void getConnection_shouldReturnValidConnection() throws Exception {
    try (Connection connection = DatabaseManager.getInstance().getConnection()) {
      assertNotNull(connection);
      assertFalse(connection.isClosed());
      assertTrue(connection.isValid(2));
    }
  }

  @Test
  void connection_shouldExecuteSimpleQuery() throws Exception {
    try (Connection connection = DatabaseManager.getInstance().getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT 1")) {

      assertTrue(resultSet.next());
      assertEquals(1, resultSet.getInt(1));
    }
  }

  @Test
  void connectionPool_shouldProvideMultipleConnections() throws Exception {
    try (Connection firstConnection = DatabaseManager.getInstance().getConnection();
        Connection secondConnection = DatabaseManager.getInstance().getConnection()) {

      assertNotNull(firstConnection);
      assertNotNull(secondConnection);
      assertFalse(firstConnection.isClosed());
      assertFalse(secondConnection.isClosed());
      assertTrue(firstConnection.isValid(2));
      assertTrue(secondConnection.isValid(2));
    }
  }
}
