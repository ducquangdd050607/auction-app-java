package com.auctionapp.auctionappjava.server.dao.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.dao.db.DatabaseManager;
import com.auctionapp.auctionappjava.server.model.Bidder;
import com.auctionapp.auctionappjava.server.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("db")
class JdbcUserDaoIntegrationTest {
  private final UserDao userDao = new JdbcUserDao();
  private UUID createdUserId;

  @AfterEach
  void cleanup() throws Exception {
    if (createdUserId == null) {
      return;
    }

    try (Connection connection = DatabaseManager.getInstance().getConnection()) {
      deleteWalletByUserId(connection, createdUserId);
      deleteUserById(connection, createdUserId);
    }
  }

  @Test
  void save_validUser_shouldBeFoundById() {
    User user = createValidUser();
    createdUserId = user.getId();

    userDao.save(user);

    Optional<User> foundUser = userDao.findById(user.getId());

    assertTrue(foundUser.isPresent());
    assertEquals(user.getUsername(), foundUser.get().getUsername());
    assertEquals(user.getEmail(), foundUser.get().getEmail());
  }

  private User createValidUser() {
    UUID userId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    User user =
        new Bidder(
            userId,
            now,
            now,
            "db_test_" + userId,
            "test-password-hash",
            "test-password-salt",
            "Database Test User",
            "db_test_" + userId + "@example.com",
            true);
    user.setRole(Role.BIDDER);
    return user;
  }

  private void deleteWalletByUserId(Connection connection, UUID userId) throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement("DELETE FROM Wallet WHERE user_id = ?")) {
      statement.setString(1, userId.toString());
      statement.executeUpdate();
    }
  }

  private void deleteUserById(Connection connection, UUID userId) throws Exception {
    try (PreparedStatement statement =
        connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
      statement.setString(1, userId.toString());
      statement.executeUpdate();
    }
  }
}
